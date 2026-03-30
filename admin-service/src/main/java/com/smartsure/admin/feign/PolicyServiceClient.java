package com.smartsure.admin.feign;

import com.smartsure.admin.dto.ApiResponse;
import com.smartsure.admin.dto.PolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "policy-service", url = "${policy.service.url:http://localhost:8082}")
public interface PolicyServiceClient {

    @GetMapping("/api/policies/{id}")
    ApiResponse<PolicyResponse> getPolicyById(@PathVariable("id") Long id);

    @PutMapping("/api/policies/{id}/status")
    ApiResponse<PolicyResponse> updatePolicyStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    @GetMapping("/api/policies")
    ApiResponse<java.util.List<PolicyResponse>> getAllPolicies();
}
