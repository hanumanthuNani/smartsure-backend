package com.smartsure.claims.service;

import com.smartsure.claims.dto.*;
import com.smartsure.claims.entity.Claim;
import com.smartsure.claims.entity.ClaimStatus;
import com.smartsure.claims.entity.ClaimType;
import com.smartsure.claims.exception.ResourceNotFoundException;
import com.smartsure.claims.feign.PolicyServiceClient;
import com.smartsure.claims.mapper.ClaimMapper;
import com.smartsure.claims.messaging.ClaimEventPublisher;
import com.smartsure.claims.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimMapper claimMapper;
    @Mock
    private PolicyServiceClient policyServiceClient;
    @Mock
    private ClaimEventPublisher claimEventPublisher;

    @InjectMocks
    private ClaimServiceImpl claimService;

    @Test
    void createClaim_Success() {
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyId(1L)
                .policyNumber("POL123")
                .claimType(ClaimType.MEDICAL)
                .build();
        PolicyResponse policy = PolicyResponse.builder()
                .id(1L)
                .policyNumber("POL123")
                .holderEmail("test@example.com")
                .status("ACTIVE")
                .build();
        
        when(policyServiceClient.getPolicyById(1L)).thenReturn(ApiResponse.success("OK", policy));
        when(claimRepository.findByPolicyId(1L)).thenReturn(Collections.emptyList());
        when(claimMapper.toEntity(any())).thenReturn(new Claim());
        when(claimRepository.save(any(Claim.class))).thenReturn(new Claim());
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<ClaimResponse> response = claimService.createClaim(request, "test@example.com", 1L);

        assertTrue(response.isSuccess());
        verify(claimEventPublisher).publishClaimEvent(any(), eq("CREATED"));
    }

    @Test
    void createClaim_PolicyInactive() {
        CreateClaimRequest request = CreateClaimRequest.builder().policyId(1L).build();
        PolicyResponse policy = PolicyResponse.builder().id(1L).status("INACTIVE").build();
        
        when(policyServiceClient.getPolicyById(1L)).thenReturn(ApiResponse.success("OK", policy));

        assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
    }

    @Test
    void createClaim_PolicyNotFound() {
        CreateClaimRequest request = CreateClaimRequest.builder().policyId(1L).build();
        when(policyServiceClient.getPolicyById(1L)).thenReturn(ApiResponse.error("Not Found"));

        assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
    }

    @Test
    void submitClaim_Success() {
        Claim claim = Claim.builder().id(1L).status(ClaimStatus.DRAFT).build();
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenReturn(claim);
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<ClaimResponse> response = claimService.submitClaim(1L, 1L);

        assertTrue(response.isSuccess());
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
    }

    @Test
    void submitClaim_InvalidStatus() {
        Claim claim = Claim.builder().id(1L).status(ClaimStatus.SUBMITTED).build();
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThrows(RuntimeException.class, () -> claimService.submitClaim(1L, 1L));
    }

    @Test
    void updateClaimStatus_UnderReview_Success() {
        Claim claim = Claim.builder().id(1L).status(ClaimStatus.SUBMITTED).build();
        UpdateClaimStatusRequest request = new UpdateClaimStatusRequest(ClaimStatus.UNDER_REVIEW, null);
        
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenReturn(claim);
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<ClaimResponse> response = claimService.updateClaimStatus(1L, request, 1L);

        assertTrue(response.isSuccess());
        assertEquals(ClaimStatus.UNDER_REVIEW, claim.getStatus());
    }

    @Test
    void updateClaimStatus_InvalidTransition() {
        Claim claim = Claim.builder().id(1L).status(ClaimStatus.DRAFT).build();
        UpdateClaimStatusRequest request = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, null);
        
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThrows(RuntimeException.class, () -> claimService.updateClaimStatus(1L, request, 1L));
    }

    @Test
    void getClaimById_NotFound() {
        when(claimRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> claimService.getClaimById(1L));
    }

    @Test
    void getClaimByNumber_Success() {
        Claim claim = Claim.builder().id(1L).claimNumber("CLM123").build();
        when(claimRepository.findByClaimNumber("CLM123")).thenReturn(Optional.of(claim));
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<ClaimResponse> response = claimService.getClaimByNumber("CLM123");

        assertTrue(response.isSuccess());
    }

    @Test
    void getAllClaims_Success() {
        when(claimRepository.findAll()).thenReturn(Collections.singletonList(new Claim()));
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<java.util.List<ClaimResponse>> response = claimService.getAllClaims();

        assertFalse(response.getData().isEmpty());
    }

    @Test
    void getClaimById_Success() {
        Claim claim = Claim.builder().id(1L).build();
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<ClaimResponse> response = claimService.getClaimById(1L);

        assertTrue(response.isSuccess());
    }

    @Test
    void getClaimsByStatus_Success() {
        when(claimRepository.findByStatus(ClaimStatus.SUBMITTED)).thenReturn(Collections.singletonList(new Claim()));
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<java.util.List<ClaimResponse>> response = claimService.getClaimsByStatus(ClaimStatus.SUBMITTED);

        assertFalse(response.getData().isEmpty());
    }

    @Test
    void createClaim_PolicyServiceUnavailable_ThrowsException() {
        CreateClaimRequest request = CreateClaimRequest.builder().policyId(1L).build();
        when(policyServiceClient.getPolicyById(1L)).thenThrow(new RuntimeException("Service Down"));

        assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
    }

    @Test
    void createClaim_PolicyNotFound_ThrowsException() {
        CreateClaimRequest request = CreateClaimRequest.builder().policyId(1L).build();
        when(policyServiceClient.getPolicyById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
    }

    @Test
    void createClaim_PolicyNotActive_ThrowsException() {
        CreateClaimRequest request = CreateClaimRequest.builder().policyId(1L).policyNumber("POL123").build();
        PolicyResponse policy = PolicyResponse.builder().id(1L).policyNumber("POL123").status("LAPSED").build();
        when(policyServiceClient.getPolicyById(1L)).thenReturn(ApiResponse.success("OK", policy));

        assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
    }

    @Test
    void createClaim_PolicyNumberMismatch_ThrowsException() {
        CreateClaimRequest request = CreateClaimRequest.builder().policyId(1L).policyNumber("WRONG").build();
        PolicyResponse policy = PolicyResponse.builder().id(1L).policyNumber("POL123").status("ACTIVE").build();
        when(policyServiceClient.getPolicyById(1L)).thenReturn(ApiResponse.success("OK", policy));

        Exception ex = assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
        assertTrue(ex.getMessage().contains("mismatch"));
    }

    @Test
    void createClaim_DuplicateClaim_ThrowsException() {
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyId(1L)
                .policyNumber("POL123")
                .claimType(ClaimType.MEDICAL)
                .build();
        PolicyResponse policy = PolicyResponse.builder()
                .id(1L)
                .policyNumber("POL123")
                .holderEmail("test@example.com")
                .status("ACTIVE")
                .build();
        
        Claim existingClaim = Claim.builder().claimType(com.smartsure.claims.entity.ClaimType.MEDICAL).status(ClaimStatus.SUBMITTED).build();

        when(policyServiceClient.getPolicyById(1L)).thenReturn(ApiResponse.success("OK", policy));
        when(claimRepository.findByPolicyId(1L)).thenReturn(Collections.singletonList(existingClaim));

        Exception ex = assertThrows(RuntimeException.class, () -> claimService.createClaim(request, "test@example.com", 1L));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void closeClaim_Success() {
        Claim claim = Claim.builder().id(1L).status(ClaimStatus.APPROVED).build();
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any())).thenReturn(claim);
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        UpdateClaimStatusRequest request = UpdateClaimStatusRequest.builder()
                .status(ClaimStatus.CLOSED)
                .build();
        
        ApiResponse<ClaimResponse> response = claimService.updateClaimStatus(1L, request, 1L);
        assertTrue(response.isSuccess());
    }

    @Test
    void deleteClaim_NotFound() {
        when(claimRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> claimService.deleteClaim(1L));
    }

    @Test
    void updateClaimStatus_Rejected_NoReason_ThrowsException() {
        Claim claim = Claim.builder().id(1L).status(ClaimStatus.UNDER_REVIEW).build();
        UpdateClaimStatusRequest request = new UpdateClaimStatusRequest(ClaimStatus.REJECTED, " ");
        
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        Exception exception = assertThrows(RuntimeException.class, () -> 
            claimService.updateClaimStatus(1L, request, 1L));
        assertEquals("Rejection reason is required when rejecting a claim", exception.getMessage());
    }

    @Test
    void getClaimsByPolicy_Success() {
        when(claimRepository.findByPolicyNumber("POL123")).thenReturn(Collections.singletonList(new Claim()));
        when(claimMapper.toResponse(any())).thenReturn(new ClaimResponse());

        ApiResponse<java.util.List<ClaimResponse>> response = claimService.getClaimsByPolicy("POL123");

        assertTrue(response.isSuccess());
        assertFalse(response.getData().isEmpty());
    }

    @Test
    void getClaimByNumber_NotFound_ThrowsException() {
        when(claimRepository.findByClaimNumber("INVALID")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> claimService.getClaimByNumber("INVALID"));
    }

    @Test
    void deleteClaim_Success() {
        when(claimRepository.existsById(1L)).thenReturn(true);
        ApiResponse<String> response = claimService.deleteClaim(1L);
        assertTrue(response.isSuccess());
        verify(claimRepository).deleteById(1L);
    }
}
