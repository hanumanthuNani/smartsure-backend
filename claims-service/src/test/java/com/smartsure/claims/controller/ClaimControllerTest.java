package com.smartsure.claims.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.ClaimResponse;
import com.smartsure.claims.dto.CreateClaimRequest;
import com.smartsure.claims.dto.UpdateClaimStatusRequest;
import com.smartsure.claims.entity.ClaimStatus;
import com.smartsure.claims.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService claimService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createClaim_Returns201() throws Exception {
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyNumber("POL123")
                .policyId(1L)
                .claimantName("John Doe")
                .claimantEmail("john@example.com")
                .claimType(com.smartsure.claims.entity.ClaimType.MEDICAL)
                .claimAmount(java.math.BigDecimal.valueOf(100.0))
                .build();
        when(claimService.createClaim(any(), any(), any())).thenReturn(ApiResponse.success("Created", new ClaimResponse()));

        mockMvc.perform(post("/api/claims")
                .header("X-UserId", "1")
                .header("X-User-Email", "john@example.com")
                .header("X-User-Roles", "ROLE_CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getClaimById_Returns200() throws Exception {
        when(claimService.getClaimById(1L)).thenReturn(ApiResponse.success("Found", new ClaimResponse()));

        mockMvc.perform(get("/api/claims/1")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void submitClaim_Returns200() throws Exception {
        when(claimService.submitClaim(any(), any())).thenReturn(ApiResponse.success("Submitted", new ClaimResponse()));

        mockMvc.perform(post("/api/claims/1/submit")
                        .header("X-UserId", "1")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void closeClaim_Returns200() throws Exception {
        when(claimService.updateClaimStatus(any(), any(), any())).thenReturn(ApiResponse.success("Closed", new ClaimResponse()));

        mockMvc.perform(post("/api/claims/1/close")
                        .header("X-UserId", "1")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getClaimByNumber_Returns200() throws Exception {
        when(claimService.getClaimByNumber("CLM123")).thenReturn(ApiResponse.success("Found", new ClaimResponse()));

        mockMvc.perform(get("/api/claims/number/CLM123")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getClaimsByStatus_Returns200() throws Exception {
        when(claimService.getClaimsByStatus(any())).thenReturn(ApiResponse.success("Success", Collections.emptyList()));

        mockMvc.perform(get("/api/claims/status/SUBMITTED")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getClaimsByPolicy_Returns200() throws Exception {
        when(claimService.getClaimsByPolicy("POL123")).thenReturn(ApiResponse.success("Success", Collections.emptyList()));

        mockMvc.perform(get("/api/claims/policy/POL123")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllClaims_Returns200() throws Exception {
        when(claimService.getAllClaims()).thenReturn(ApiResponse.success("All", Collections.emptyList()));

        mockMvc.perform(get("/api/claims")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateClaimStatus_Returns200() throws Exception {
        UpdateClaimStatusRequest request = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, null);
        when(claimService.updateClaimStatus(eq(1L), any(), any()))
                .thenReturn(ApiResponse.success("Updated", new ClaimResponse()));

        mockMvc.perform(put("/api/claims/1/status")
                .header("X-UserId", "1")
                .header("X-User-Roles", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteClaim_Returns200() throws Exception {
        when(claimService.deleteClaim(1L)).thenReturn(ApiResponse.success("Deleted", "OK"));

        mockMvc.perform(delete("/api/claims/1")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
