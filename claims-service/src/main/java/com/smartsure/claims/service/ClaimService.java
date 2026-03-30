package com.smartsure.claims.service;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.ClaimResponse;
import com.smartsure.claims.dto.CreateClaimRequest;
import com.smartsure.claims.dto.FullClaimResponse;
import com.smartsure.claims.dto.UpdateClaimStatusRequest;
import com.smartsure.claims.entity.ClaimStatus;

import java.util.List;

public interface ClaimService {
    ApiResponse<ClaimResponse> createClaim(CreateClaimRequest request, String claimantEmail, Long creatorId);
    ApiResponse<ClaimResponse> submitClaim(Long id, Long userId);
    ApiResponse<ClaimResponse> updateClaimStatus(Long id, UpdateClaimStatusRequest request, Long userId);
    ApiResponse<ClaimResponse> getClaimById(Long id);
    ApiResponse<FullClaimResponse> getFullClaimById(Long id);
    ApiResponse<ClaimResponse> getClaimByNumber(String claimNumber);
    ApiResponse<List<ClaimResponse>> getAllClaims();
    ApiResponse<List<ClaimResponse>> getAllClaimsForAdmin();
    ApiResponse<List<ClaimResponse>> getClaimsByEmail(String email);
    ApiResponse<List<ClaimResponse>> getClaimsByStatus(ClaimStatus status);
    ApiResponse<List<ClaimResponse>> getClaimsByPolicy(String policyNumber);
    ApiResponse<String> deleteClaim(Long id);
}
