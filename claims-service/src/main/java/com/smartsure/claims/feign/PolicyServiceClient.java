package com.smartsure.claims.feign;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.PolicyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "policy-service")
public interface PolicyServiceClient {

    @GetMapping("/api/policies/{id}")
    ApiResponse<PolicyResponse> getPolicyById(@PathVariable("id") Long id);
}