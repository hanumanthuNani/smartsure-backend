package com.smartsure.policy.service;

import com.smartsure.policy.dto.ApiResponse;
import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.dto.PurchasePolicyRequest;
import com.smartsure.policy.entity.PolicyStatus;

import java.util.List;

public interface PolicyService {
    ApiResponse<PolicyResponse> createPolicy(CreatePolicyRequest request);
    ApiResponse<PolicyResponse> purchasePolicy(PurchasePolicyRequest request);
    ApiResponse<PolicyResponse> getPolicyById(Long id);
    ApiResponse<PolicyResponse> getPolicyByNumber(String policyNumber);
    ApiResponse<List<PolicyResponse>> getPoliciesByEmail(String email);
    ApiResponse<List<PolicyResponse>> getPoliciesByStatus(PolicyStatus status);
    ApiResponse<List<PolicyResponse>> getAllPolicies();
    ApiResponse<PolicyResponse> updatePolicyStatus(Long id, PolicyStatus status);
    ApiResponse<Void> deletePolicy(Long id);

    // Policy Plans (Offerings)
    ApiResponse<List<com.smartsure.policy.entity.PolicyPlan>> getAllPolicyPlans();
    ApiResponse<List<com.smartsure.policy.entity.PolicyPlan>> getPolicyPlansByType(com.smartsure.policy.entity.PolicyType type);
}
