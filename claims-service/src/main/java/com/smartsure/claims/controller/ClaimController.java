package com.smartsure.claims.controller;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.ClaimResponse;
import com.smartsure.claims.dto.CreateClaimRequest;
import com.smartsure.claims.dto.FullClaimResponse;
import com.smartsure.claims.dto.UpdateClaimStatusRequest;
import com.smartsure.claims.entity.ClaimStatus;
import com.smartsure.claims.service.ClaimService;
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
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@Tag(name = "Claims Management", description = "Endpoints for managing insurance claims")
public class ClaimController {

    private final ClaimService claimService;
    private final HttpServletRequest request;

    private String getRoles() { return request.getHeader("X-User-Roles"); }
    private String getEmail() { return request.getHeader("X-User-Email"); }
    private String getUserId() { return request.getHeader("X-UserId"); }
    private boolean isAdmin() { return getRoles() != null && getRoles().contains("ROLE_ADMIN"); }

    private <T> ResponseEntity<ApiResponse<T>> validateAdmin() {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Admin role required"));
        }
        return null;
    }

    private <T> ResponseEntity<ApiResponse<T>> validateOwnership(Long claimId) {
        if (isAdmin()) return null;
        
        ApiResponse<ClaimResponse> claimResponse = claimService.getClaimById(claimId);
        if (claimResponse.getData() == null || !claimResponse.getData().getClaimantEmail().equals(getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You do not own this claim"));
        }
        return null;
    }

    @PostMapping
    @Operation(summary = "👤 [CUSTOMER] Create a new claim", description = "Initiates a claim for an active policy. Requires customer ownership.")
    public ResponseEntity<ApiResponse<ClaimResponse>> createClaim(@Valid @RequestBody CreateClaimRequest createRequest) {
        log.info("REST request to create claim for policy: {} by user: {}", createRequest.getPolicyId(), getEmail());
        Long userId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<ClaimResponse> response = claimService.createClaim(createRequest, getEmail(), userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "👤 [OWNER] Submit a claim", description = "Finalizes and submits a drafted claim. REQUIRES AT LEAST 1 UPLOADED PROOF DOCUMENT.")
    public ResponseEntity<ApiResponse<ClaimResponse>> submitClaim(@PathVariable Long id) {
        ResponseEntity<ApiResponse<ClaimResponse>> authResponse = validateOwnership(id);
        if (authResponse != null) return authResponse;

        log.info("REST request to submit claim: {}", id);
        Long userId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<ClaimResponse> response = claimService.submitClaim(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "🔒 [ADMIN] Update claim status", description = "Admins can APPROVE or REJECT a submitted claim.")
    public ResponseEntity<ApiResponse<ClaimResponse>> updateClaimStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClaimStatusRequest statusUpdateLabel) {
        ResponseEntity<ApiResponse<ClaimResponse>> authResponse = validateAdmin();
        if (authResponse != null) return authResponse;

        log.info("REST request to update status for claim: {}", id);
        Long userId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<ClaimResponse> response = claimService.updateClaimStatus(id, statusUpdateLabel, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "👤 [OWNER] Close a claim", description = "Allows the user to manually withdraw or close their claim.")
    public ResponseEntity<ApiResponse<ClaimResponse>> closeClaim(@PathVariable Long id) {
        ResponseEntity<ApiResponse<ClaimResponse>> authResponse = validateOwnership(id);
        if (authResponse != null) return authResponse;

        log.info("REST request to close claim: {}", id);
        UpdateClaimStatusRequest closeRequest = new UpdateClaimStatusRequest();
        closeRequest.setStatus(ClaimStatus.CLOSED);
        Long userId = (getUserId() != null) ? Long.parseLong(getUserId()) : 0L;
        ApiResponse<ClaimResponse> response = claimService.updateClaimStatus(id, closeRequest, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "🔒 [ADMIN] Get ALL claims", description = "Comprehensive list of all claims across the system for admins.")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getAllClaimsForAdmin() {
        ResponseEntity<ApiResponse<List<ClaimResponse>>> authResponse = validateAdmin();
        if (authResponse != null) return authResponse;

        log.info("Admin request to fetch ALL claims for verification");
        return ResponseEntity.ok(claimService.getAllClaimsForAdmin());
    }

    @GetMapping
    @Operation(summary = "👤 [CUSTOMER] Get my claims", description = "Lists all claims associated with the current user's email.")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getAllClaims() {
        log.info("User {} fetching their claims", getEmail());
        return ResponseEntity.ok(claimService.getClaimsByEmail(getEmail()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "👤 [OWNER] Get claim by ID", description = "Fetches specific claim details. Requires ownership or admin status.")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimById(@PathVariable Long id) {
        ResponseEntity<ApiResponse<ClaimResponse>> authResponse = validateOwnership(id);
        if (authResponse != null) return authResponse;

        log.info("REST request to get claim by ID: {}", id);
        ApiResponse<ClaimResponse> response = claimService.getClaimById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/full")
    @Operation(summary = "👤 [OWNER] Get full claim history", description = "Detailed audit trail of claim status transitions.")
    public ResponseEntity<ApiResponse<FullClaimResponse>> getFullClaimById(@PathVariable Long id) {
        ResponseEntity<ApiResponse<FullClaimResponse>> authResponse = validateOwnership(id);
        if (authResponse != null) return authResponse;

        log.info("REST request to get FULL claim details for ID: {}", id);
        return ResponseEntity.ok(claimService.getFullClaimById(id));
    }

    @GetMapping("/number/{claimNumber}")
    @Operation(summary = "👤 [OWNER] Search claim by number", description = "Finds a claim using its unique CLM identifier.")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimByNumber(@PathVariable String claimNumber) {
        log.info("REST request to get claim by number: {}", claimNumber);
        ApiResponse<ClaimResponse> response = claimService.getClaimByNumber(claimNumber);
        
        if (!isAdmin() && (response.getData() == null || !response.getData().getClaimantEmail().equals(getEmail()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You do not own this claim"));
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "🔒 [ADMIN] Filter claims by status", description = "Admins can view all claims pending PENDING, APPROVED, or REJECTED.")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getClaimsByStatus(@PathVariable ClaimStatus status) {
        ResponseEntity<ApiResponse<List<ClaimResponse>>> authResponse = validateAdmin();
        if (authResponse != null) return authResponse;

        log.info("REST request to get claims by status: {}", status);
        ApiResponse<List<ClaimResponse>> response = claimService.getClaimsByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policy/{policyNumber}")
    @Operation(summary = "👤 [OWNER] Get claims for specific policy", description = "Filters the user's claims based on a policy number.")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getClaimsByPolicy(@PathVariable String policyNumber) {
        log.info("REST request to get claims by policy number: {}", policyNumber);
        ApiResponse<List<ClaimResponse>> response = claimService.getClaimsByPolicy(policyNumber);
        
        if (!isAdmin()) {
            List<ClaimResponse> filtered = response.getData().stream()
                    .filter(c -> c.getClaimantEmail().equals(getEmail()))
                    .toList();
            return ResponseEntity.ok(ApiResponse.success("Claims fetched and filtered", filtered));
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "🔒 [ADMIN] Delete claim", description = "Permanently removes a claim from the system. Admin only.")
    public ResponseEntity<ApiResponse<String>> deleteClaim(@PathVariable Long id) {
        ResponseEntity<ApiResponse<String>> authResponse = validateAdmin();
        if (authResponse != null) return authResponse;

        log.info("REST request to delete claim ID: {}", id);
        ApiResponse<String> response = claimService.deleteClaim(id);
        return ResponseEntity.ok(response);
    }
}
