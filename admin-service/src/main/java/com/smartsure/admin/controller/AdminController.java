package com.smartsure.admin.controller;

import com.smartsure.admin.dto.AdminActionResponse;
import com.smartsure.admin.dto.ApiResponse;
import com.smartsure.admin.dto.ClaimActionRequest;
import com.smartsure.admin.dto.ClaimResponse;
import com.smartsure.admin.dto.FullClaimResponse;
import com.smartsure.admin.dto.PolicyActionRequest;
import com.smartsure.admin.dto.ReportResponse;
import com.smartsure.admin.service.AdminService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Endpoints for high-level administrative actions across claims and policies")
public class AdminController {

    private final AdminService adminService;
    private final HttpServletRequest request;

    private String getUserId() {
        return request.getHeader("X-UserId");
    }

    private <T> ResponseEntity<ApiResponse<T>> validateAdminRole() {
        String roles = request.getHeader("X-User-Roles");
        if (roles == null || !roles.contains("ROLE_ADMIN")) {
            log.warn("Access Denied: User does not have ROLE_ADMIN. Roles found: {}", roles);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You Are Not Authorized"));
        }
        return null;
    }

    @PostMapping("/claims/approve")
    @Operation(summary = "🔒 [ADMIN] Approve a submitted claim", description = "Moves a claim to APPROVED status and triggers payout workflows.")
    public ResponseEntity<ApiResponse<AdminActionResponse>> approveClaim(@Valid @RequestBody ClaimActionRequest request) {
        ResponseEntity<ApiResponse<AdminActionResponse>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to approve claim ID: {}", request.getClaimId());
        Long adminId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<AdminActionResponse> response = adminService.approveClaim(request.getClaimId(), request.getReason(), adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/claims/reject")
    @Operation(summary = "🔒 [ADMIN] Reject a submitted claim", description = "Marks a claim as REJECTED with a mandatory reason for the claimant.")
    public ResponseEntity<ApiResponse<AdminActionResponse>> rejectClaim(@Valid @RequestBody ClaimActionRequest request) {
        ResponseEntity<ApiResponse<AdminActionResponse>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to reject claim ID: {}", request.getClaimId());
        Long adminId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<AdminActionResponse> response = adminService.rejectClaim(request.getClaimId(), request.getReason(), adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/claims/verify-list")
    @Operation(summary = "🔒 [ADMIN] View claims pending verification", description = "Returns a list of all claims in SUBMITTED status waiting for admin review.")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getClaimsForVerification() {
        ResponseEntity<ApiResponse<List<ClaimResponse>>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to fetch all claims for verification");
        return ResponseEntity.ok(adminService.getClaimsForVerification());
    }

    @GetMapping("/claims/{id}/proofs")
    @Operation(summary = "🔒 [ADMIN] Audit claim proofs", description = "Fetches a claim along with all uploaded document metadata for detailed audit.")
    public ResponseEntity<ApiResponse<FullClaimResponse>> getClaimWithProofs(@PathVariable Long id) {
        ResponseEntity<ApiResponse<FullClaimResponse>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to get claim ID: {} with proofs", id);
        return ResponseEntity.ok(adminService.getClaimWithProofs(id));
    }

    @PostMapping("/policies/activate")
    @Operation(summary = "🔒 [ADMIN] Manually activate a policy", description = "Used for special overrides or late payments to set a policy to ACTIVE.")
    public ResponseEntity<ApiResponse<AdminActionResponse>> activatePolicy(@Valid @RequestBody PolicyActionRequest request) {
        ResponseEntity<ApiResponse<AdminActionResponse>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to activate policy ID: {}", request.getPolicyId());
        Long adminId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<AdminActionResponse> response = adminService.activatePolicy(request.getPolicyId(), request.getReason(), adminId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/policies/deactivate")
    @Operation(summary = "🔒 [ADMIN] Deactivate/Cancel a policy", description = "Suspends coverage for a policy. Used for fraud detection or user requests.")
    public ResponseEntity<ApiResponse<AdminActionResponse>> deactivatePolicy(@Valid @RequestBody PolicyActionRequest request) {
        ResponseEntity<ApiResponse<AdminActionResponse>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to deactivate policy ID: {}", request.getPolicyId());
        Long adminId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<AdminActionResponse> response = adminService.deactivatePolicy(request.getPolicyId(), request.getReason(), adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actions")
    @Operation(summary = "🔒 [ADMIN] View audit trail", description = "Returns a complete history of all administrative actions performed across the system.")
    public ResponseEntity<ApiResponse<List<AdminActionResponse>>> getAllActions() {
        ResponseEntity<ApiResponse<List<AdminActionResponse>>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to get all admin actions");
        ApiResponse<List<AdminActionResponse>> response = adminService.getAllActions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actions/admin/{adminId}")
    @Operation(summary = "🔒 [ADMIN] View actions by specific admin", description = "Filters the audit trail for a specific administrator ID.")
    public ResponseEntity<ApiResponse<List<AdminActionResponse>>> getActionsByAdmin(@PathVariable Long adminId) {
        ResponseEntity<ApiResponse<List<AdminActionResponse>>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to get admin actions by admin ID: {}", adminId);
        ApiResponse<List<AdminActionResponse>> response = adminService.getActionsByAdmin(adminId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/actions/target/{targetId}")
    @Operation(summary = "🔒 [ADMIN] View actions for specific entity", description = "Filters the audit trail for all actions taken on a specific claim or policy.")
    public ResponseEntity<ApiResponse<List<AdminActionResponse>>> getActionsByTarget(@PathVariable Long targetId) {
        ResponseEntity<ApiResponse<List<AdminActionResponse>>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to get admin actions by target ID: {}", targetId);
        ApiResponse<List<AdminActionResponse>> response = adminService.getActionsByTarget(targetId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reports")
    @Operation(summary = "🔒 [ADMIN] Generate system intelligence report", description = "Aggregates global statistics on total claims, payout ratios, and active policies.")
    public ResponseEntity<ApiResponse<ReportResponse>> generateReport() {
        ResponseEntity<ApiResponse<ReportResponse>> authResponse = validateAdminRole();
        if (authResponse != null) return authResponse;

        log.info("REST request to generate system report");
        ApiResponse<ReportResponse> response = adminService.generateReport();
        return ResponseEntity.ok(response);
    }
}
