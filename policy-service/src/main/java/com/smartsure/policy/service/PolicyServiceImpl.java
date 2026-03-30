package com.smartsure.policy.service;

import com.smartsure.policy.dto.ApiResponse;
import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.dto.PurchasePolicyRequest;
import com.smartsure.policy.entity.Policy;
import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.exception.ResourceNotFoundException;
import com.smartsure.policy.mapper.PolicyMapper;
import com.smartsure.policy.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import io.github.resilience4j.retry.annotation.Retry;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
    private final PremiumCalculator premiumCalculator;
    private final RabbitTemplate rabbitTemplate;
    private final com.smartsure.policy.repository.PolicyPlanRepository policyPlanRepository;

    @Override
    @Transactional
    @CacheEvict(value = "policies", allEntries = true)
    public ApiResponse<PolicyResponse> createPolicy(CreatePolicyRequest request) {
        log.info("Creating policy for holder: {}", request.getHolderName());
        
        checkDuplicatePolicy(request.getHolderEmail(), request.getPolicyType());

        Policy policy = policyMapper.toPolicy(request);
        policy.setPremium(premiumCalculator.calculate(request.getPolicyType(), request.getCoverageAmount()));
        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy created successfully with ID: {}", savedPolicy.getId());
        return ApiResponse.success("Policy created successfully", policyMapper.toResponse(savedPolicy));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "policies", allEntries = true),
        @CacheEvict(value = "policy", allEntries = true)
    })
    public ApiResponse<PolicyResponse> purchasePolicy(PurchasePolicyRequest request) {
        log.info("Purchasing policy for holder: {}", request.getHolderName());
        
        checkDuplicatePolicy(request.getHolderEmail(), request.getPolicyType());

        Policy policy = policyMapper.toPolicy(request);
        policy.setPremium(premiumCalculator.calculate(request.getPolicyType(), request.getCoverageAmount()));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now());
        policy.setEndDate(LocalDate.now().plusYears(1));
                
        Policy savedPolicy = policyRepository.save(policy);
        log.info("Policy purchased successfully with ID: {}", savedPolicy.getId());

        // ✅ SAGA STEP: Publish event for eventual consistency (Email notification)
        try {
            rabbitTemplate.convertAndSend("policy.exchange", "policy.purchase", policyMapper.toResponse(savedPolicy));
            log.info("Sent policy purchase event to RabbitMQ");
        } catch (Exception e) {
            log.error("Failed to send policy purchase event", e);
        }

        return ApiResponse.success("Policy purchased successfully", policyMapper.toResponse(savedPolicy));
    }

    @Override
    @Cacheable(value = "policy", key = "#id")
    @CircuitBreaker(name = "policyService", fallbackMethod = "getPolicyByIdFallback")
    @Retry(name = "policyService")
    public ApiResponse<PolicyResponse> getPolicyById(Long id) {
        log.info("Fetching policy by ID: {}", id);
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + id));
        return ApiResponse.success("Policy fetched successfully", policyMapper.toResponse(policy));
    }

    public ApiResponse<PolicyResponse> getPolicyByIdFallback(Long id, Exception ex) {
        log.error("Circuit breaker triggered for policy ID: {}", id, ex);
        return ApiResponse.error("Policy service temporarily unavailable. Try again later.");
    }

    @Override
    @Cacheable(value = "policy", key = "#policyNumber")
    public ApiResponse<PolicyResponse> getPolicyByNumber(String policyNumber) {
        log.info("Fetching policy by number: {}", policyNumber);
        Policy policy = policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with number: " + policyNumber));
        return ApiResponse.success("Policy fetched successfully", policyMapper.toResponse(policy));
    }

    @Override
    public ApiResponse<List<PolicyResponse>> getPoliciesByEmail(String email) {
        log.info("Fetching policies for email: {}", email);
        List<PolicyResponse> responses = policyRepository.findByHolderEmail(email).stream()
                .map(policyMapper::toResponse)
                .toList();
        return ApiResponse.success("Policies fetched for user", responses);
    }

    @Override
    @Cacheable("policies")
    public ApiResponse<List<PolicyResponse>> getAllPolicies() {
        log.info("Fetching all policies");
        List<PolicyResponse> responses = policyRepository.findAll().stream()
                .map(policyMapper::toResponse)
                .toList();
        return ApiResponse.success("Policies fetched successfully", responses);
    }

    @Override
    public ApiResponse<List<PolicyResponse>> getPoliciesByStatus(PolicyStatus status) {
        log.info("Fetching policies by status: {}", status);
        List<PolicyResponse> responses = policyRepository.findByStatus(status).stream()
                .map(policyMapper::toResponse)
                .toList();
        return ApiResponse.success("Policies fetched successfully", responses);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "policies", allEntries = true),
        @CacheEvict(value = "policy", key = "#id")
    })
    public ApiResponse<PolicyResponse> updatePolicyStatus(Long id, PolicyStatus status) {
        log.info("Updating status for policy ID: {} to {}", id, status);
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found with ID: " + id));
        policy.setStatus(status);
        Policy updatedPolicy = policyRepository.save(policy);
        return ApiResponse.success("Policy status updated successfully", policyMapper.toResponse(updatedPolicy));
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "policies", allEntries = true),
        @CacheEvict(value = "policy", key = "#id")
    })
    public ApiResponse<Void> deletePolicy(Long id) {
        log.info("Deleting policy with ID: {}", id);
        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Policy not found with ID: " + id);
        }
        policyRepository.deleteById(id);
        return ApiResponse.success("Policy deleted successfully", null);
    }

    @Override
    @Cacheable("plans")
    public ApiResponse<List<com.smartsure.policy.entity.PolicyPlan>> getAllPolicyPlans() {
        log.info("Fetching all available policy plans");
        return ApiResponse.success("Policy plans fetched", policyPlanRepository.findAll());
    }

    @Override
    @Cacheable(value = "plans", key = "#type")
    public ApiResponse<List<com.smartsure.policy.entity.PolicyPlan>> getPolicyPlansByType(com.smartsure.policy.entity.PolicyType type) {
        log.info("Fetching policy plans for type: {}", type);
        return ApiResponse.success("Plans fetched for " + type, policyPlanRepository.findByPolicyType(type));
    }

    private void checkDuplicatePolicy(String email, com.smartsure.policy.entity.PolicyType type) {
        boolean exists = policyRepository.findByHolderEmail(email).stream()
                .anyMatch(p -> p.getPolicyType() == type && p.getStatus() == PolicyStatus.ACTIVE);
        
        if (exists) {
            log.warn("User {} already has an active policy of type {}", email, type);
            throw new RuntimeException("An active policy of type " + type + " already exists for this user.");
        }
    }
}
