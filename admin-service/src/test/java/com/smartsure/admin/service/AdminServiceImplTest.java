package com.smartsure.admin.service;

import com.smartsure.admin.dto.*;
import com.smartsure.admin.entity.AdminAction;
import com.smartsure.admin.feign.ClaimsServiceClient;
import com.smartsure.admin.feign.PolicyServiceClient;
import com.smartsure.admin.mapper.AdminMapper;
import com.smartsure.admin.exception.AdminServiceException;
import com.smartsure.admin.exception.ResourceNotFoundException;
import com.smartsure.admin.messaging.AdminEventPublisher;
import com.smartsure.admin.repository.AdminActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminActionRepository adminActionRepository;
    @Mock
    private AdminMapper adminMapper;
    @Mock
    private ClaimsServiceClient claimsServiceClient;
    @Mock
    private PolicyServiceClient policyServiceClient;
    @Mock
    private AdminEventPublisher adminEventPublisher;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void approveClaim_Success() {
        Long claimId = 1L;
        String reason = "Approve reason";
        Long adminId = 100L;

        ClaimResponse claim = ClaimResponse.builder().id(claimId).status("SUBMITTED").build();
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(ApiResponse.success("Found", claim));
        when(adminActionRepository.save(any(AdminAction.class))).thenReturn(new AdminAction());
        when(adminMapper.toResponse(any())).thenReturn(new AdminActionResponse());

        ApiResponse<AdminActionResponse> response = adminService.approveClaim(claimId, reason, adminId);

        assertTrue(response.isSuccess());
        verify(claimsServiceClient).updateClaimStatus(eq(claimId), any(UpdateClaimStatusRequest.class));
        verify(adminActionRepository).save(any(AdminAction.class));
    }

    @Test
    void rejectClaim_Success() {
        Long claimId = 1L;
        String reason = "Reject reason";
        Long adminId = 100L;

        ClaimResponse claim = ClaimResponse.builder().id(claimId).status("SUBMITTED").build();
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(ApiResponse.success("Found", claim));
        when(adminActionRepository.save(any(AdminAction.class))).thenReturn(new AdminAction());

        ApiResponse<AdminActionResponse> response = adminService.rejectClaim(claimId, reason, adminId);

        assertTrue(response.isSuccess());
        verify(claimsServiceClient).updateClaimStatus(eq(claimId), any(UpdateClaimStatusRequest.class));
    }

    @Test
    void getReports_Success() {
        List<PolicyResponse> policies = Arrays.asList(
            PolicyResponse.builder().status("ACTIVE").build(),
            PolicyResponse.builder().status("INACTIVE").build()
        );
        List<ClaimResponse> claims = Arrays.asList(
            ClaimResponse.builder().status("SUBMITTED").build(),
            ClaimResponse.builder().status("APPROVED").build(),
            ClaimResponse.builder().status("REJECTED").build()
        );

        when(policyServiceClient.getAllPolicies()).thenReturn(ApiResponse.success("OK", policies));
        when(claimsServiceClient.getAllClaimsForAdmin()).thenReturn(ApiResponse.success("OK", claims));
        when(adminActionRepository.count()).thenReturn(5L);

        ApiResponse<ReportResponse> response = adminService.generateReport();

        assertTrue(response.isSuccess());
        ReportResponse data = response.getData();
        assertEquals(2, data.getTotalPolicies());
        assertEquals(1, data.getActivePolicies());
        assertEquals(3, data.getTotalClaims());
        assertEquals(1, data.getPendingClaims());
        assertEquals(1, data.getApprovedClaims());
        assertEquals(1, data.getRejectedClaims());
        assertEquals(5, data.getTotalAdminActions());
    }

    @Test
    void approveClaim_NotFound() {
        Long claimId = 1L;
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(ApiResponse.error("Not Found"));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> 
            adminService.approveClaim(claimId, "Reason", 1L));
        
        assertTrue(exception.getMessage().contains("Claim not found"));
    }

    @Test
    void activatePolicy_Success() {
        Long policyId = 1L;
        PolicyResponse policy = PolicyResponse.builder().id(policyId).build();
        when(policyServiceClient.getPolicyById(policyId)).thenReturn(ApiResponse.success("OK", policy));
        when(adminActionRepository.save(any(AdminAction.class))).thenReturn(new AdminAction());

        ApiResponse<AdminActionResponse> response = adminService.activatePolicy(policyId, "Reason", 1L);

        assertTrue(response.isSuccess());
        verify(policyServiceClient).updatePolicyStatus(policyId, "ACTIVE");
    }

    @Test
    void deactivatePolicy_Success() {
        Long policyId = 1L;
        PolicyResponse policy = PolicyResponse.builder().id(policyId).build();
        when(policyServiceClient.getPolicyById(policyId)).thenReturn(ApiResponse.success("OK", policy));
        when(adminActionRepository.save(any(AdminAction.class))).thenReturn(new AdminAction());

        ApiResponse<AdminActionResponse> response = adminService.deactivatePolicy(policyId, "Reason", 1L);

        assertTrue(response.isSuccess());
        verify(policyServiceClient).updatePolicyStatus(policyId, "INACTIVE");
    }
    @Test
    void approveClaim_UnderReview_Success() {
        Long claimId = 1L;
        ClaimResponse claim = ClaimResponse.builder().id(claimId).status("UNDER_REVIEW").build();
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(ApiResponse.success("Found", claim));
        when(adminActionRepository.save(any(AdminAction.class))).thenReturn(new AdminAction());

        ApiResponse<AdminActionResponse> response = adminService.approveClaim(claimId, "Reason", 1L);

        assertTrue(response.isSuccess());
        verify(claimsServiceClient).updateClaimStatus(eq(claimId), any());
    }

    @Test
    void approveClaim_InvalidStatus() {
        Long claimId = 1L;
        ClaimResponse claim = ClaimResponse.builder().id(claimId).status("APPROVED").build();
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(ApiResponse.success("Found", claim));

        Exception exception = assertThrows(IllegalStateException.class, () -> 
            adminService.approveClaim(claimId, "Reason", 1L));
        
        assertTrue(exception.getMessage().contains("approvable state"));
    }

    @Test
    void approveClaim_NullResponse() {
        Long claimId = 1L;
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> adminService.approveClaim(claimId, "Reason", 1L));
    }

    @Test
    void getActionsByAdmin_Success() {
        when(adminActionRepository.findByAdminId(1L)).thenReturn(Collections.emptyList());
        ApiResponse<List<AdminActionResponse>> response = adminService.getActionsByAdmin(1L);
        assertTrue(response.isSuccess());
    }

    @Test
    void getActionsByTarget_Success() {
        when(adminActionRepository.findByTargetId(1L)).thenReturn(Collections.emptyList());
        ApiResponse<List<AdminActionResponse>> response = adminService.getActionsByTarget(1L);
        assertTrue(response.isSuccess());
    }

    @Test
    void getAllActions_Success() {
        when(adminActionRepository.findAll()).thenReturn(Collections.emptyList());
        ApiResponse<List<AdminActionResponse>> response = adminService.getAllActions();
        assertTrue(response.isSuccess());
    }

    @Test
    void generateReport_Empty_Success() {
        when(policyServiceClient.getAllPolicies()).thenReturn(null);
        when(claimsServiceClient.getAllClaimsForAdmin()).thenReturn(ApiResponse.error("Fail"));
        when(adminActionRepository.count()).thenReturn(0L);

        ApiResponse<ReportResponse> response = adminService.generateReport();

        assertTrue(response.isSuccess());
        assertEquals(0, response.getData().getTotalPolicies());
    }

    @Test
    void rejectClaim_BlankReason() {
        assertThrows(IllegalArgumentException.class, () -> 
            adminService.rejectClaim(1L, " ", 100L));
    }

    @Test
    void approveClaim_GeneralError() {
        when(claimsServiceClient.getClaimById(anyLong())).thenThrow(new RuntimeException("API Down"));
        assertThrows(AdminServiceException.class, () -> 
            adminService.approveClaim(1L, "Reason", 1L));
    }

    @Test
    void rejectClaim_NotFound() {
        Long claimId = 1L;
        when(claimsServiceClient.getClaimById(claimId)).thenReturn(ApiResponse.error("Not Found"));
        assertThrows(ResourceNotFoundException.class, () -> 
            adminService.rejectClaim(claimId, "Reason", 1L));
    }

    @Test
    void rejectClaim_GeneralError() {
        when(claimsServiceClient.getClaimById(anyLong())).thenThrow(new RuntimeException("API Down"));
        assertThrows(AdminServiceException.class, () -> 
            adminService.rejectClaim(1L, "Reason", 1L));
    }

    @Test
    void activatePolicy_NotFound() {
        when(policyServiceClient.getPolicyById(anyLong())).thenReturn(ApiResponse.error("Fail"));
        assertThrows(ResourceNotFoundException.class, () -> 
            adminService.activatePolicy(1L, "Reason", 1L));
    }

    @Test
    void activatePolicy_GeneralError() {
        when(policyServiceClient.getPolicyById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(AdminServiceException.class, () -> 
            adminService.activatePolicy(1L, "Reason", 1L));
    }

    @Test
    void deactivatePolicy_NotFound() {
        when(policyServiceClient.getPolicyById(anyLong())).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> 
            adminService.deactivatePolicy(1L, "Reason", 1L));
    }

    @Test
    void deactivatePolicy_GeneralError() {
        when(policyServiceClient.getPolicyById(anyLong())).thenThrow(new RuntimeException("Error"));
        assertThrows(AdminServiceException.class, () -> 
            adminService.deactivatePolicy(1L, "Reason", 1L));
    }

    @Test
    void getClaimsForVerification_Success() {
        when(claimsServiceClient.getAllClaimsForAdmin()).thenReturn(ApiResponse.success("OK", Collections.emptyList()));
        ApiResponse<List<ClaimResponse>> response = adminService.getClaimsForVerification();
        assertTrue(response.isSuccess());
    }

    @Test
    void getClaimWithProofs_Success() {
        when(claimsServiceClient.getFullClaimById(1L)).thenReturn(ApiResponse.success("OK", new FullClaimResponse()));
        ApiResponse<FullClaimResponse> response = adminService.getClaimWithProofs(1L);
        assertTrue(response.isSuccess());
    }

    @Test
    void claimsFallback_Success() {
        ApiResponse<List<ClaimResponse>> response = adminService.claimsFallback(new RuntimeException("Error"));
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("busy"));
    }

    @Test
    void fullClaimFallback_Success() {
        ApiResponse<FullClaimResponse> response = adminService.fullClaimFallback(1L, new RuntimeException("Error"));
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("unable to fetch"));
    }
}
