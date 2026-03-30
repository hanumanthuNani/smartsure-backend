package com.smartsure.policy.service;

import com.smartsure.policy.dto.ApiResponse;
import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.dto.PurchasePolicyRequest;
import com.smartsure.policy.entity.Policy;
import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.entity.PolicyType;
import com.smartsure.policy.exception.ResourceNotFoundException;
import com.smartsure.policy.mapper.PolicyMapper;
import com.smartsure.policy.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyMapper policyMapper;

    @Mock
    private PremiumCalculator premiumCalculator;

    @InjectMocks
    private PolicyServiceImpl policyService;

    @Test
    void createPolicy_success() {
        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setHolderName("John Doe");
        request.setPolicyType(PolicyType.HEALTH);
        request.setCoverageAmount(new BigDecimal("100000"));

        Policy policy = new Policy();
        policy.setId(1L);

        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findByHolderEmail(any())).willReturn(List.of());
        given(policyMapper.toPolicy(request)).willReturn(policy);
        given(premiumCalculator.calculate(PolicyType.HEALTH, new BigDecimal("100000"))).willReturn(new BigDecimal("2000.00"));
        given(policyRepository.save(policy)).willReturn(policy);
        given(policyMapper.toResponse(policy)).willReturn(responseDto);

        ApiResponse<PolicyResponse> result = policyService.createPolicy(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(premiumCalculator).calculate(PolicyType.HEALTH, new BigDecimal("100000"));
        verify(policyRepository).save(policy);
    }

    @Test
    void purchasePolicy_success() {
        PurchasePolicyRequest request = new PurchasePolicyRequest();
        request.setHolderName("John Doe");
        request.setPolicyType(PolicyType.HEALTH);
        request.setCoverageAmount(new BigDecimal("100000"));

        Policy savedPolicy = new Policy();
        savedPolicy.setId(1L);
        savedPolicy.setStatus(PolicyStatus.ACTIVE);

        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findByHolderEmail(any())).willReturn(List.of());
        given(policyMapper.toPolicy(any(PurchasePolicyRequest.class))).willReturn(new Policy());
        given(premiumCalculator.calculate(PolicyType.HEALTH, new BigDecimal("100000"))).willReturn(new BigDecimal("2000.00"));
        given(policyRepository.save(any(Policy.class))).willReturn(savedPolicy);
        given(policyMapper.toResponse(savedPolicy)).willReturn(responseDto);

        ApiResponse<PolicyResponse> result = policyService.purchasePolicy(request);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    void getPolicyById_found() {
        Policy policy = new Policy();
        policy.setId(1L);
        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findById(1L)).willReturn(Optional.of(policy));
        given(policyMapper.toResponse(policy)).willReturn(responseDto);

        ApiResponse<PolicyResponse> result = policyService.getPolicyById(1L);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getPolicyById_notFound() {
        given(policyRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.getPolicyById(99L));
    }

    @Test
    void getPolicyByNumber_found() {
        Policy policy = new Policy();
        policy.setPolicyNumber("POL-ABC");
        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findByPolicyNumber("POL-ABC")).willReturn(Optional.of(policy));
        given(policyMapper.toResponse(policy)).willReturn(responseDto);

        ApiResponse<PolicyResponse> result = policyService.getPolicyByNumber("POL-ABC");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
    }

    @Test
    void getPolicyByNumber_notFound() {
        given(policyRepository.findByPolicyNumber("POL-XXX")).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.getPolicyByNumber("POL-XXX"));
    }

    @Test
    void getAllPolicies_success() {
        Policy policy1 = new Policy();
        Policy policy2 = new Policy();
        PolicyResponse res1 = new PolicyResponse();
        PolicyResponse res2 = new PolicyResponse();

        given(policyRepository.findAll()).willReturn(List.of(policy1, policy2));
        given(policyMapper.toResponse(policy1)).willReturn(res1);
        given(policyMapper.toResponse(policy2)).willReturn(res2);

        ApiResponse<List<PolicyResponse>> result = policyService.getAllPolicies();

        assertTrue(result.isSuccess());
        assertEquals(2, result.getData().size());
    }

    @Test
    void updatePolicyStatus_success() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.INACTIVE);
        
        Policy updatedPolicy = new Policy();
        updatedPolicy.setId(1L);
        updatedPolicy.setStatus(PolicyStatus.ACTIVE);
        
        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findById(1L)).willReturn(Optional.of(policy));
        given(policyRepository.save(policy)).willReturn(updatedPolicy);
        given(policyMapper.toResponse(updatedPolicy)).willReturn(responseDto);

        ApiResponse<PolicyResponse> result = policyService.updatePolicyStatus(1L, PolicyStatus.ACTIVE);

        assertTrue(result.isSuccess());
        verify(policyRepository).save(policy);
    }

    @Test
    void updatePolicyStatus_notFound() {
        given(policyRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> policyService.updatePolicyStatus(99L, PolicyStatus.ACTIVE));
    }

    @Test
    void deletePolicy_success() {
        given(policyRepository.existsById(1L)).willReturn(true);

        ApiResponse<Void> result = policyService.deletePolicy(1L);

        assertTrue(result.isSuccess());
        verify(policyRepository).deleteById(1L);
    }

    @Test
    void deletePolicy_notFound() {
        given(policyRepository.existsById(99L)).willReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> policyService.deletePolicy(99L));
    }

    @Test
    void getPolicyByIdFallback_returnsError() {
        ApiResponse<PolicyResponse> response = policyService.getPolicyByIdFallback(1L, new RuntimeException("Test"));
        assertFalse(response.isSuccess());
        assertEquals("Policy service temporarily unavailable. Try again later.", response.getMessage());
    }

    @Test
    void getPoliciesByStatus_success() {
        Policy policy = new Policy();
        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findByStatus(PolicyStatus.ACTIVE)).willReturn(List.of(policy));
        given(policyMapper.toResponse(policy)).willReturn(responseDto);

        ApiResponse<List<PolicyResponse>> result = policyService.getPoliciesByStatus(PolicyStatus.ACTIVE);

        assertTrue(result.isSuccess());
        assertFalse(result.getData().isEmpty());
    }

    @Test
    void createPolicy_duplicate_throwsException() {
        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setHolderEmail("test@example.com");
        request.setPolicyType(PolicyType.HEALTH);

        Policy existingPolicy = new Policy();
        existingPolicy.setPolicyType(PolicyType.HEALTH);
        existingPolicy.setStatus(PolicyStatus.ACTIVE);

        given(policyRepository.findByHolderEmail("test@example.com")).willReturn(List.of(existingPolicy));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> policyService.createPolicy(request));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void getPoliciesByEmail_success() {
        Policy policy = new Policy();
        PolicyResponse responseDto = new PolicyResponse();

        given(policyRepository.findByHolderEmail("test@example.com")).willReturn(List.of(policy));
        given(policyMapper.toResponse(policy)).willReturn(responseDto);

        ApiResponse<List<PolicyResponse>> result = policyService.getPoliciesByEmail("test@example.com");

        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
    }
}
