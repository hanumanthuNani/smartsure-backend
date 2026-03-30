package com.smartsure.claims.service;

import com.smartsure.claims.dto.*;
import com.smartsure.claims.entity.Claim;
import com.smartsure.claims.entity.ClaimStatus;
import com.smartsure.claims.exception.ClaimsServiceException;
import com.smartsure.claims.exception.ResourceNotFoundException;
import com.smartsure.claims.feign.PolicyServiceClient;
import com.smartsure.claims.mapper.ClaimMapper;
import com.smartsure.claims.messaging.ClaimEventPublisher;
import com.smartsure.claims.messaging.EmailEventPublisher;
import com.smartsure.claims.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
    private final PolicyServiceClient policyServiceClient;
    private final ClaimEventPublisher claimEventPublisher;
    private final DocumentService documentService;
    private final EmailEventPublisher emailEventPublisher;

    private static final String CLAIM_NOT_FOUND_MSG = "Claim not found with ID: ";
    private static final String CLAIMS_FETCHED_MSG = "Claims fetched successfully";

    @Override
    @Transactional
    @CacheEvict(value = {"claims", "claim"}, allEntries = true)
    public ApiResponse<ClaimResponse> createClaim(CreateClaimRequest request, String email, Long userId) {
        log.info("Creating claim for policy ID: {} by user: {}", request.getPolicyId(), email);

        ApiResponse<PolicyResponse> policyResponse;
        try {
            policyResponse = policyServiceClient.getPolicyById(request.getPolicyId());
        } catch (Exception e) {
            log.error("Error communicating with Policy Service", e);
            throw new ClaimsServiceException("Policy Service error: " + e.getMessage(), e);
        }

        if (policyResponse == null || !policyResponse.isSuccess() || policyResponse.getData() == null) {
            throw new ClaimsServiceException("Policy not found with ID: " + request.getPolicyId());
        }

        PolicyResponse policy = policyResponse.getData();
        
        // 1. Validate Policy Number match
        if (!policy.getPolicyNumber().equalsIgnoreCase(request.getPolicyNumber())) {
            throw new ClaimsServiceException("Policy number mismatch for given policy ID");
        }

        // 2. Validate Policy Holder (if not internal or if we want strict ownership)
        // Note: For now we trust the email passed from controller, but we verify it matches the policy's holder
        if (email != null && !email.equals(policy.getHolderEmail())) {
             log.warn("User {} attempted to create claim for policy owned by {}", email, policy.getHolderEmail());
             throw new ClaimsServiceException("You do not own this policy");
        }

        // 3. Ensure Policy is ACTIVE
        if (!"ACTIVE".equals(policy.getStatus())) {
            throw new ClaimsServiceException("Policy must be ACTIVE to create a claim. Current status: " + policy.getStatus());
        }

        // 4. Duplicate Check
        checkDuplicateClaim(request.getPolicyId(), com.smartsure.claims.entity.ClaimType.valueOf(request.getClaimType().name()));

        Claim claim = claimMapper.toEntity(request);
        claim.setClaimantEmail(policy.getHolderEmail()); // Ensure it uses the policy's email
        claim.setCreatedBy(userId);
        claim.setStatus(ClaimStatus.DRAFT);
        
        Claim savedClaim = claimRepository.save(claim);
        ClaimResponse claimResponseData = claimMapper.toResponse(savedClaim);
        
        claimEventPublisher.publishClaimEvent(claimResponseData, "CREATED");
        log.info("Successfully created claim ID: {}", savedClaim.getId());
        
        return ApiResponse.success("Claim created successfully", claimResponseData);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"claims", "claim"}, allEntries = true)
    public ApiResponse<ClaimResponse> submitClaim(Long id, Long userId) {
        log.info("Submitting claim ID: {}", id);
        
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLAIM_NOT_FOUND_MSG + id));
                
        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT claims can be submitted");
        }

        // Enforce proof (at least 1 document)
        if (documentService.getDocumentsByClaim(id).getData() == null || 
            documentService.getDocumentsByClaim(id).getData().isEmpty()) {
            log.warn("Claim submission blocked: No documents found for claim ID: {}", id);
            throw new ClaimsServiceException("Proof required: Please upload at least 1 document before submitting your claim.");
        }
        
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());
        
        Claim savedClaim = claimRepository.save(claim);
        ClaimResponse claimResponseData = claimMapper.toResponse(savedClaim);
        
        claimEventPublisher.publishClaimEvent(claimResponseData, "SUBMITTED");
        
        // Send Notification
        emailEventPublisher.sendEmailNotification(savedClaim.getClaimantEmail(), 
            "We've Received Your Claim - SmartSure Insurance 🛡️", 
            "Hello,\n\n" +
            "Your claim #" + savedClaim.getClaimNumber() + " has been successfully submitted. We understand that this may be a stressful time, and we are here to support you.\n\n" +
            "Our dedicated claims team is already beginning the review process. We will carefully verify your documents and get back to you with an update as soon as possible.\n\n" +
            "Thank you for your patience and for trusting SmartSure with your protection.\n\n" +
            "Warm regards,\n" +
            "The SmartSure Claims Team");

        log.info("Successfully submitted claim ID: {}", id);
        
        return ApiResponse.success("Claim submitted successfully", claimResponseData);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"claims", "claim"}, allEntries = true)
    public ApiResponse<ClaimResponse> updateClaimStatus(Long id, UpdateClaimStatusRequest request, Long userId) {
        log.info("Updating status for claim ID: {} to {}", id, request.getStatus());
        
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLAIM_NOT_FOUND_MSG + id));
                
        ClaimStatus currentStatus = claim.getStatus();
        ClaimStatus newStatus = request.getStatus();
        
        boolean validTransition = (currentStatus == ClaimStatus.SUBMITTED && newStatus == ClaimStatus.UNDER_REVIEW) ||
                                (currentStatus == ClaimStatus.UNDER_REVIEW && (newStatus == ClaimStatus.APPROVED || newStatus == ClaimStatus.REJECTED)) ||
                                ((currentStatus == ClaimStatus.APPROVED || currentStatus == ClaimStatus.REJECTED) && newStatus == ClaimStatus.CLOSED);
        
        if (!validTransition) {
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
        
        if (newStatus == ClaimStatus.REJECTED && (request.getRejectionReason() == null || request.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting a claim");
        }
        
        if (newStatus == ClaimStatus.APPROVED || newStatus == ClaimStatus.REJECTED) {
            claim.setReviewedAt(LocalDateTime.now());
        }
        
        claim.setStatus(newStatus);
        if (request.getRejectionReason() != null) {
            claim.setRejectionReason(request.getRejectionReason());
        }
        
        Claim savedClaim = claimRepository.save(claim);
        ClaimResponse claimResponseData = claimMapper.toResponse(savedClaim);
        
        claimEventPublisher.publishClaimEvent(claimResponseData, newStatus.name().toLowerCase());
        log.info("Successfully updated status to {} for claim ID: {}", newStatus, id);
        
        return ApiResponse.success("Claim status updated successfully", claimResponseData);
    }

    @Override
    @Cacheable(value = "claim", key = "#id")
    public ApiResponse<ClaimResponse> getClaimById(Long id) {
        log.info("Fetching claim by ID: {}", id);
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLAIM_NOT_FOUND_MSG + id));
        return ApiResponse.success(CLAIMS_FETCHED_MSG, claimMapper.toResponse(claim));
    }

    @Override
    public ApiResponse<FullClaimResponse> getFullClaimById(Long id) {
        log.info("Fetching full claim details for ID: {}", id);
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CLAIM_NOT_FOUND_MSG + id));
        
        ClaimResponse claimResponse = claimMapper.toResponse(claim);
        ApiResponse<List<DocumentResponse>> documentsResponse = documentService.getDocumentsByClaim(id);
        
        return ApiResponse.success("Full claim details fetched successfully", 
                FullClaimResponse.builder()
                        .claim(claimResponse)
                        .documents(documentsResponse.getData())
                        .build());
    }

    @Override
    @Cacheable(value = "claim", key = "#claimNumber")
    public ApiResponse<ClaimResponse> getClaimByNumber(String claimNumber) {
        log.info("Fetching claim by number: {}", claimNumber);
        Claim claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with number: " + claimNumber));
        return ApiResponse.success("Claim fetched successfully", claimMapper.toResponse(claim));
    }

    @Override
    @Cacheable(value = "claims", key = "#email")
    public ApiResponse<List<ClaimResponse>> getClaimsByEmail(String email) {
        log.info("Fetching claims for user: {}", email);
        List<ClaimResponse> responses = claimRepository.findByClaimantEmail(email).stream()
                .map(claimMapper::toResponse)
                .toList();
        return ApiResponse.success(CLAIMS_FETCHED_MSG, responses);
    }

    @Override
    @Cacheable(value = "claims")
    public ApiResponse<List<ClaimResponse>> getAllClaims() {
        log.info("Fetching all claims (internal/filtered)");
        List<ClaimResponse> responses = claimRepository.findAll().stream()
                .map(claimMapper::toResponse)
                .toList();
        return ApiResponse.success(CLAIMS_FETCHED_MSG, responses);
    }

    @Override
    public ApiResponse<List<ClaimResponse>> getAllClaimsForAdmin() {
        log.info("Admin fetching all submitted claims for verification");
        // We can prioritize SUBMITTED and UNDER_REVIEW claims here if needed
        List<ClaimResponse> responses = claimRepository.findAll().stream()
                .map(claimMapper::toResponse)
                .toList();
        return ApiResponse.success("All claims fetched for Admin review", responses);
    }

    @Override
    public ApiResponse<List<ClaimResponse>> getClaimsByStatus(ClaimStatus status) {
        log.info("Fetching claims by status: {}", status);
        List<ClaimResponse> responses = claimRepository.findByStatus(status).stream()
                .map(claimMapper::toResponse)
                .toList();
        return ApiResponse.success(CLAIMS_FETCHED_MSG, responses);
    }

    @Override
    public ApiResponse<List<ClaimResponse>> getClaimsByPolicy(String policyNumber) {
        log.info("Fetching claims by policy number: {}", policyNumber);
        List<ClaimResponse> responses = claimRepository.findByPolicyNumber(policyNumber).stream()
                .map(claimMapper::toResponse)
                .toList();
        return ApiResponse.success(CLAIMS_FETCHED_MSG, responses);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"claims", "claim"}, allEntries = true)
    public ApiResponse<String> deleteClaim(Long id) {
        log.info("Deleting claim ID: {}", id);
        if (!claimRepository.existsById(id)) {
            throw new ResourceNotFoundException(CLAIM_NOT_FOUND_MSG + id);
        }
        claimRepository.deleteById(id);
        return ApiResponse.success("Claim deleted successfully", "Deleted claim ID: " + id);
    }

    private void checkDuplicateClaim(Long policyId, com.smartsure.claims.entity.ClaimType claimType) {
        List<Claim> existingClaims = claimRepository.findByPolicyId(policyId);
        boolean duplicateExists = existingClaims.stream()
                .anyMatch(c -> c.getClaimType() == claimType && 
                          c.getStatus() != ClaimStatus.REJECTED && 
                          c.getStatus() != ClaimStatus.CLOSED);
        
        if (duplicateExists) {
            log.warn("Duplicate claim attempt for policy ID: {} and type: {}", policyId, claimType);
            throw new ClaimsServiceException("A claim of type " + claimType + " already exists for this policy");
        }
    }
}
