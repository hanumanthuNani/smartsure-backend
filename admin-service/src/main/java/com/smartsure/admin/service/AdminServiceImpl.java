package com.smartsure.admin.service;

import com.smartsure.admin.dto.AdminActionResponse;
import com.smartsure.admin.dto.ApiResponse;
import com.smartsure.admin.dto.ClaimResponse;
import com.smartsure.admin.dto.ClaimStatus;
import com.smartsure.admin.dto.FullClaimResponse;
import com.smartsure.admin.dto.PolicyResponse;
import com.smartsure.admin.dto.ReportResponse;
import com.smartsure.admin.dto.UpdateClaimStatusRequest;
import com.smartsure.admin.exception.AdminServiceException;
import com.smartsure.admin.exception.ResourceNotFoundException;
import com.smartsure.admin.entity.AdminAction;
import com.smartsure.admin.entity.AdminActionType;
import com.smartsure.admin.feign.ClaimsServiceClient;
import com.smartsure.admin.feign.PolicyServiceClient;
import com.smartsure.admin.messaging.AdminEventPublisher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import com.smartsure.admin.mapper.AdminMapper;
import com.smartsure.admin.repository.AdminActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminActionRepository adminActionRepository;
    private final AdminMapper adminMapper;
    private final ClaimsServiceClient claimsServiceClient;
    private final PolicyServiceClient policyServiceClient;
    private final AdminEventPublisher adminEventPublisher;

    @Override
    @CacheEvict(value = "adminActions", allEntries = true)
    @CircuitBreaker(name = "claimsService", fallbackMethod = "fallbackApproveClaim")
    @Retry(name = "claimsService")
    public ApiResponse<AdminActionResponse> approveClaim(Long claimId, String reason, Long adminId) {
        log.info("Admin {} approving claim ID: {} with reason: {}", adminId, claimId, reason);
        try {
            ApiResponse<ClaimResponse> claimResponse = claimsServiceClient.getClaimById(claimId);
            if (claimResponse == null || !claimResponse.isSuccess() || claimResponse.getData() == null) {
                throw new ResourceNotFoundException("Claim not found");
            }

            String status = claimResponse.getData().getStatus();
            if (!"SUBMITTED".equals(status) && !"UNDER_REVIEW".equals(status)) {
                throw new IllegalStateException("Claim is not in an approvable state");
            }

            UpdateClaimStatusRequest request = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, null);
            claimsServiceClient.updateClaimStatus(claimId, request);

            AdminAction action = AdminAction.builder()
                    .adminId(adminId)
                    .actionType(AdminActionType.APPROVE_CLAIM)
                    .targetId(claimId)
                    .targetType("CLAIM")
                    .reason(reason)
                    .build();

            AdminAction savedAction = adminActionRepository.save(action);

            // Send Notification
            if (claimResponse.getData() != null) {
                String email = claimResponse.getData().getClaimantEmail();
                adminEventPublisher.sendEmailNotification(email,
                    "Good News! Your Claim is Approved - SmartSure Insurance 🛡️",
                    "Hello,\n\n" +
                    "We are pleased to inform you that your claim #" + claimResponse.getData().getClaimNumber() + " has been officially APPROVED.\n\n" +
                    "At SmartSure, we are committed to being there when you need us most. Our team has completed the verification, and we are now initiating the next steps for your settlement.\n\n" +
                    "Details:\n" +
                    "- Claim Number: " + claimResponse.getData().getClaimNumber() + "\n" +
                    "- Status: Approved\n" +
                    "- Administrator's Note: " + reason + "\n\n" +
                    "If you have any further questions, please do not hesitate to reach out. Thank you for choosing SmartSure.\n\n" +
                    "Best regards,\n" +
                    "The SmartSure Team");
            }

            return ApiResponse.success("Claim approved successfully", adminMapper.toResponse(savedAction));
        } catch (ResourceNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error approving claim ID: {}", claimId, e);
            throw new AdminServiceException("Failed to approve claim: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "adminActions", allEntries = true)
    @CircuitBreaker(name = "claimsService", fallbackMethod = "fallbackRejectClaim")
    @Retry(name = "claimsService")
    public ApiResponse<AdminActionResponse> rejectClaim(Long claimId, String reason, Long adminId) {
        log.info("Admin {} rejecting claim ID: {} with reason: {}", adminId, claimId, reason);
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason cannot be blank");
        }

        try {
            ApiResponse<ClaimResponse> claimResponse = claimsServiceClient.getClaimById(claimId);
            if (claimResponse == null || !claimResponse.isSuccess() || claimResponse.getData() == null) {
                throw new ResourceNotFoundException("Claim not found");
            }

            UpdateClaimStatusRequest request = new UpdateClaimStatusRequest(ClaimStatus.REJECTED, reason);
            claimsServiceClient.updateClaimStatus(claimId, request);

            AdminAction action = AdminAction.builder()
                    .adminId(adminId)
                    .actionType(AdminActionType.REJECT_CLAIM)
                    .targetId(claimId)
                    .targetType("CLAIM")
                    .reason(reason)
                    .build();

            AdminAction savedAction = adminActionRepository.save(action);

            // Send Notification
            if (claimResponse.getData() != null) {
                String email = claimResponse.getData().getClaimantEmail();
                adminEventPublisher.sendEmailNotification(email,
                    "Claim Update - SmartSure Insurance",
                    "Status Update: Your claim #" + claimResponse.getData().getClaimNumber() + " has been rejected.\nReason: " + reason);
            }

            return ApiResponse.success("Claim rejected successfully", adminMapper.toResponse(savedAction));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting claim ID: {}", claimId, e);
            throw new AdminServiceException("Failed to reject claim: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "adminActions", allEntries = true)
    @CircuitBreaker(name = "policyService", fallbackMethod = "fallbackActivatePolicy")
    @Retry(name = "policyService")
    public ApiResponse<AdminActionResponse> activatePolicy(Long policyId, String reason, Long adminId) {
        log.info("Admin {} activating policy ID: {} with reason: {}", adminId, policyId, reason);
        try {
            ApiResponse<PolicyResponse> policyResponse = policyServiceClient.getPolicyById(policyId);
            if (policyResponse == null || !policyResponse.isSuccess() || policyResponse.getData() == null) {
                throw new ResourceNotFoundException("Policy not found");
            }

            policyServiceClient.updatePolicyStatus(policyId, "ACTIVE");

            AdminAction action = AdminAction.builder()
                    .adminId(adminId)
                    .actionType(AdminActionType.ACTIVATE_POLICY)
                    .targetId(policyId)
                    .targetType("POLICY")
                    .reason(reason)
                    .build();

            AdminAction savedAction = adminActionRepository.save(action);

            // Send Notification
            if (policyResponse.getData() != null) {
                adminEventPublisher.sendEmailNotification(policyResponse.getData().getHolderEmail(),
                    "Policy Activated - SmartSure Insurance",
                    "Congratulations! Your policy #" + policyResponse.getData().getPolicyNumber() + " is now ACTIVE and coverage has started.");
            }

            return ApiResponse.success("Policy activated successfully", adminMapper.toResponse(savedAction));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error activating policy ID: {}", policyId, e);
            throw new AdminServiceException("Failed to activate policy: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "adminActions", allEntries = true)
    @CircuitBreaker(name = "policyService", fallbackMethod = "fallbackDeactivatePolicy")
    @Retry(name = "policyService")
    public ApiResponse<AdminActionResponse> deactivatePolicy(Long policyId, String reason, Long adminId) {
        log.info("Admin {} deactivating policy ID: {} with reason: {}", adminId, policyId, reason);
        try {
            ApiResponse<PolicyResponse> policyResponse = policyServiceClient.getPolicyById(policyId);
            if (policyResponse == null || !policyResponse.isSuccess() || policyResponse.getData() == null) {
                throw new ResourceNotFoundException("Policy not found");
            }

            policyServiceClient.updatePolicyStatus(policyId, "INACTIVE");

            AdminAction action = AdminAction.builder()
                    .adminId(adminId)
                    .actionType(AdminActionType.DEACTIVATE_POLICY)
                    .targetId(policyId)
                    .targetType("POLICY")
                    .reason(reason)
                    .build();

            AdminAction savedAction = adminActionRepository.save(action);

            // Send Notification
            if (policyResponse.getData() != null) {
                adminEventPublisher.sendEmailNotification(policyResponse.getData().getHolderEmail(),
                    "Policy Deactivated - SmartSure Insurance",
                    "Alert: Your policy #" + policyResponse.getData().getPolicyNumber() + " has been deactivated.\nReason: " + reason);
            }

            return ApiResponse.success("Policy deactivated successfully", adminMapper.toResponse(savedAction));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deactivating policy ID: {}", policyId, e);
            throw new AdminServiceException("Failed to deactivate policy: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "adminActions")
    public ApiResponse<List<AdminActionResponse>> getActionsByAdmin(Long adminId) {
        log.info("Fetching actions for admin ID: {}", adminId);
        List<AdminActionResponse> responses = adminActionRepository.findByAdminId(adminId).stream()
                .map(adminMapper::toResponse)
                .toList();
        return ApiResponse.success("Actions fetched successfully", responses);
    }

    @Override
    @CircuitBreaker(name = "claimsService", fallbackMethod = "claimsFallback")
    @Retry(name = "claimsService")
    public ApiResponse<List<ClaimResponse>> getClaimsForVerification() {
        log.info("Fetching all claims for admin verification");
        return claimsServiceClient.getAllClaimsForAdmin();
    }

    @Override
    @CircuitBreaker(name = "claimsService", fallbackMethod = "fullClaimFallback")
    @Retry(name = "claimsService")
    public ApiResponse<FullClaimResponse> getClaimWithProofs(Long claimId) {
        log.info("Fetching full claim details including proofs for ID: {}", claimId);
        return claimsServiceClient.getFullClaimById(claimId);
    }

    @Override
    public ApiResponse<List<AdminActionResponse>> getActionsByTarget(Long targetId) {
        log.info("Fetching actions for target ID: {}", targetId);
        List<AdminActionResponse> responses = adminActionRepository.findByTargetId(targetId).stream()
                .map(adminMapper::toResponse)
                .toList();
        return ApiResponse.success("Actions fetched successfully", responses);
    }

    @Override
    @Cacheable(value = "adminActions")
    public ApiResponse<List<AdminActionResponse>> getAllActions() {
        log.info("Fetching all actions");
        List<AdminActionResponse> responses = adminActionRepository.findAll().stream()
                .map(adminMapper::toResponse)
                .toList();
        return ApiResponse.success("All actions fetched successfully", responses);
    }

    @Override
    @Cacheable(value = "report")
    @CircuitBreaker(name = "reportGenerator", fallbackMethod = "fallbackGenerateReport")
    public ApiResponse<ReportResponse> generateReport() {
        log.info("Generating system-wide report");
        
        ApiResponse<List<PolicyResponse>> policies = policyServiceClient.getAllPolicies();
        ApiResponse<List<ClaimResponse>> claims = claimsServiceClient.getAllClaimsForAdmin();
        
        long totalPolicies = (policies.getData() != null) ? policies.getData().size() : 0;
        long activePolicies = (policies.getData() != null) ? policies.getData().stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .count() : 0;

        long totalClaims = (claims.getData() != null) ? claims.getData().size() : 0;
        long pendingClaims = (claims.getData() != null) ? claims.getData().stream()
                .filter(c -> "SUBMITTED".equals(c.getStatus()) || "UNDER_REVIEW".equals(c.getStatus()))
                .count() : 0;
        long approvedClaims = (claims.getData() != null) ? claims.getData().stream()
                .filter(c -> "APPROVED".equals(c.getStatus()))
                .count() : 0;
        long rejectedClaims = (claims.getData() != null) ? claims.getData().stream()
                .filter(c -> "REJECTED".equals(c.getStatus()))
                .count() : 0;
                
        long totalAdminActions = adminActionRepository.count();

        ReportResponse reportResponse = ReportResponse.builder()
                .totalPolicies(totalPolicies)
                .activePolicies(activePolicies)
                .totalClaims(totalClaims)
                .pendingClaims(pendingClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .totalAdminActions(totalAdminActions)
                .build();

        return ApiResponse.success("Report generated", reportResponse);
    }

    // --- Fallback Methods ---

    public ApiResponse<AdminActionResponse> fallbackApproveClaim(Long claimId, String reason, Long adminId, Throwable t) {
        log.error("Fallback for approveClaim due to: {}", t.getMessage());
        return ApiResponse.error("Claims Service is currently unavailable. Please try again later.");
    }

    public ApiResponse<AdminActionResponse> fallbackRejectClaim(Long claimId, String reason, Long adminId, Throwable t) {
        log.error("Fallback for rejectClaim due to: {}", t.getMessage());
        return ApiResponse.error("Claims Service is currently unavailable. Please try again later.");
    }

    public ApiResponse<List<ClaimResponse>> claimsFallback(Exception e) {
        log.error("Claims Service fallback triggered: {}", e.getMessage());
        return ApiResponse.error("Claims Service is currently busy. Please try later.");
    }

    public ApiResponse<FullClaimResponse> fullClaimFallback(Long claimId, Exception e) {
        log.error("Full Claim detail fallback triggered for ID {}: {}", claimId, e.getMessage());
        return ApiResponse.error("Unable to fetch claim proofs at this time.");
    }

    public ApiResponse<AdminActionResponse> fallbackActivatePolicy(Long policyId, String reason, Long adminId, Throwable t) {
        log.error("Fallback for activatePolicy due to: {}", t.getMessage());
        return ApiResponse.error("Policy Service is currently unavailable. Please try again later.");
    }

    public ApiResponse<AdminActionResponse> fallbackDeactivatePolicy(Long policyId, String reason, Long adminId, Throwable t) {
        log.error("Fallback for deactivatePolicy due to: {}", t.getMessage());
        return ApiResponse.error("Policy Service is currently unavailable. Please try again later.");
    }

    public ApiResponse<ReportResponse> fallbackGenerateReport(Throwable t) {
        log.error("Fallback for generateReport due to: {}", t.getMessage());
        return ApiResponse.error("System report could not be generated as one or more services are unreachable.");
    }
}
