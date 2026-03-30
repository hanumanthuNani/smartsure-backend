package com.smartsure.admin.feign;

import com.smartsure.admin.dto.ApiResponse;
import com.smartsure.admin.dto.ClaimResponse;
import com.smartsure.admin.dto.FullClaimResponse;
import com.smartsure.admin.dto.UpdateClaimStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "claims-service", url = "${claims.service.url:http://localhost:8083}")
public interface ClaimsServiceClient {

    @GetMapping("/api/claims/{id}")
    ApiResponse<ClaimResponse> getClaimById(@PathVariable("id") Long id);

    @GetMapping("/api/claims/{id}/full")
    ApiResponse<FullClaimResponse> getFullClaimById(@PathVariable("id") Long id);

    @PutMapping("/api/claims/{id}/status")
    ApiResponse<ClaimResponse> updateClaimStatus(@PathVariable("id") Long id, @RequestBody UpdateClaimStatusRequest request);

    @GetMapping("/api/claims/all")
    ApiResponse<List<ClaimResponse>> getAllClaimsForAdmin();
}
