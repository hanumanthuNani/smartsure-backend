package com.smartsure.admin.service;

import com.smartsure.admin.dto.AdminActionResponse;
import com.smartsure.admin.dto.ApiResponse;
import com.smartsure.admin.dto.ClaimResponse;
import com.smartsure.admin.dto.FullClaimResponse;
import com.smartsure.admin.dto.ReportResponse;

import java.util.List;

public interface AdminService {
    ApiResponse<AdminActionResponse> approveClaim(Long claimId, String reason, Long adminId);
    ApiResponse<AdminActionResponse> rejectClaim(Long claimId, String reason, Long adminId);
    ApiResponse<AdminActionResponse> activatePolicy(Long policyId, String reason, Long adminId);
    ApiResponse<AdminActionResponse> deactivatePolicy(Long policyId, String reason, Long adminId);

    ApiResponse<List<ClaimResponse>> getClaimsForVerification();
    ApiResponse<FullClaimResponse> getClaimWithProofs(Long claimId);

    ApiResponse<List<AdminActionResponse>> getActionsByAdmin(Long adminId);
    ApiResponse<List<AdminActionResponse>> getActionsByTarget(Long targetId);
    ApiResponse<List<AdminActionResponse>> getAllActions();
    ApiResponse<ReportResponse> generateReport();
}
