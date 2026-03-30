package com.smartsure.policy.controller;

import com.smartsure.policy.dto.ApiResponse;
import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.dto.PurchasePolicyRequest;
import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "Endpoints for managing insurance policies and viewing plans")
public class PolicyController {

    private final PolicyService policyService;
    private final HttpServletRequest request;

    private String getRoles() {
        return request.getHeader("X-User-Roles");
    }

    private String getEmail() {
        return request.getHeader("X-User-Email");
    }

    private String getUserId() {
        return request.getHeader("X-UserId");
    }

    private boolean isAdmin() {
        return getRoles() != null && getRoles().contains("ROLE_ADMIN");
    }

    private <T> ResponseEntity<ApiResponse<T>> validateAdmin() {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Admin role required"));
        }
        return null;
    }

    private <T> ResponseEntity<ApiResponse<T>> validateOwnership(Long policyId) {
        if (isAdmin())
            return null;

        ApiResponse<PolicyResponse> policyResponse = policyService.getPolicyById(policyId);
        if (policyResponse.getData() == null || (getEmail() != null && !policyResponse.getData().getHolderEmail().equals(getEmail()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You do not own this policy"));
        }
        return null;
    }

    @PostMapping
    @Operation(summary = "🔒 [ADMIN] Create a manual policy", description = "Allows admins to manually bypass payment and create a policy for a user.")
    public ResponseEntity<ApiResponse<PolicyResponse>> createPolicy(
            @Valid @RequestBody CreatePolicyRequest createRequest) {
        ResponseEntity<ApiResponse<PolicyResponse>> authResponse = validateAdmin();
        if (authResponse != null)
            return authResponse;

        log.info("Admin request to create policy for holder: {}", createRequest.getHolderName());

        if (!isAdmin()) {
            createRequest.setHolderEmail(getEmail());
        }

        if (getUserId() != null) {
            createRequest.setCreatedBy(Long.parseLong(getUserId()));
        }

        ApiResponse<PolicyResponse> response = policyService.createPolicy(createRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/purchase")
    @Operation(summary = "👤 [CUSTOMER] Purchase a policy", description = "Purchases an insurance plan. Requires valid customer session.")
    public ResponseEntity<ApiResponse<PolicyResponse>> purchasePolicy(
            @Valid @RequestBody PurchasePolicyRequest purchaseRequest) {
        log.info("REST request to purchase policy for holder: {} by user: {}", purchaseRequest.getHolderName(),
                getEmail());

        if (!isAdmin()) {
            purchaseRequest.setHolderEmail(getEmail());
        }

        if (getUserId() != null) {
            purchaseRequest.setCreatedBy(Long.parseLong(getUserId()));
        }

        ApiResponse<PolicyResponse> response = policyService.purchasePolicy(purchaseRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "👥 [ADMIN/CUSTOMER] Get all policies", description = "Admins see all; Customers see only their own.")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> getAllPolicies() {
        if (isAdmin()) {
            log.info("Admin fetching all policies");
            return ResponseEntity.ok(policyService.getAllPolicies());
        }
        log.info("User {} fetching their policies", getEmail());
        return ResponseEntity.ok(policyService.getPoliciesByEmail(getEmail()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "👤 [OWNER] Get policy by ID", description = "Fetches details of a specific policy. User must be the owner or admin.")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicyById(@PathVariable Long id) {
        ResponseEntity<ApiResponse<PolicyResponse>> authResponse = validateOwnership(id);
        if (authResponse != null)
            return authResponse;

        log.info("REST request to get policy by ID: {}", id);
        ApiResponse<PolicyResponse> response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{policyNumber}")
    @Operation(summary = "👤 [OWNER] Get policy by Number", description = "Fetches a policy using its unique POL number.")
    public ResponseEntity<ApiResponse<PolicyResponse>> getPolicyByNumber(@PathVariable String policyNumber) {
        log.info("REST request to get policy by number: {}", policyNumber);
        ApiResponse<PolicyResponse> response = policyService.getPolicyByNumber(policyNumber);

        if (!isAdmin() && (response.getData() == null || !response.getData().getHolderEmail().equals(getEmail()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You do not own this policy"));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "🔒 [ADMIN] Get policies by status", description = "Admins can filter policies by ACTIVE, EXPIRED, or CANCELLED.")
    public ResponseEntity<ApiResponse<List<PolicyResponse>>> getPoliciesByStatus(@PathVariable PolicyStatus status) {
        ResponseEntity<ApiResponse<List<PolicyResponse>>> authResponse = validateAdmin();
        if (authResponse != null)
            return authResponse;

        log.info("REST request to get policies by status: {}", status);
        ApiResponse<List<PolicyResponse>> response = policyService.getPoliciesByStatus(status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "🔒 [ADMIN] Update policy status", description = "Admins can manually change a policy's status.")
    public ResponseEntity<ApiResponse<PolicyResponse>> updatePolicyStatus(
            @PathVariable Long id,
            @RequestParam PolicyStatus status) {
        ResponseEntity<ApiResponse<PolicyResponse>> authResponse = validateAdmin();
        if (authResponse != null)
            return authResponse;

        log.info("REST request to update policy {} status to {}", id, status);
        ApiResponse<PolicyResponse> response = policyService.updatePolicyStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "🔒 [ADMIN] Delete policy", description = "Completely removes a policy record from the system.")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(@PathVariable Long id) {
        ResponseEntity<ApiResponse<Void>> authResponse = validateAdmin();
        if (authResponse != null)
            return authResponse;

        log.info("REST request to delete policy ID: {}", id);
        ApiResponse<Void> response = policyService.deletePolicy(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans")
    @Operation(summary = "🌐 [PUBLIC] View all policy plans", description = "Shows PolicyBazaar-style Gold/Silver/Platinum plans for all categories.")
    public ResponseEntity<ApiResponse<List<com.smartsure.policy.entity.PolicyPlan>>> getAllPolicyPlans() {
        log.info("REST request to fetch all policy plans");
        return ResponseEntity.ok(policyService.getAllPolicyPlans());
    }

    @GetMapping("/plans/{type}")
    @Operation(summary = "🌐 [PUBLIC] View plans by category", description = "Filter plans by HEALTH, LIFE, VEHICLE, or PROPERTY.")
    public ResponseEntity<ApiResponse<List<com.smartsure.policy.entity.PolicyPlan>>> getPolicyPlansByType(@PathVariable com.smartsure.policy.entity.PolicyType type) {
        log.info("REST request to fetch policy plans for type: {}", type);
        return ResponseEntity.ok(policyService.getPolicyPlansByType(type));
    }
}
