package com.smartsure.policy.controller;

import com.smartsure.policy.dto.ApiResponse;
import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.dto.PurchasePolicyRequest;
import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.service.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPolicy_returns201() throws Exception {
        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .holderName("John")
                .holderEmail("john@test.com")
                .policyType(com.smartsure.policy.entity.PolicyType.HEALTH)
                .coverageAmount(new java.math.BigDecimal("1000"))
                .premium(new java.math.BigDecimal("20"))
                .build();
        
        given(policyService.createPolicy(any(CreatePolicyRequest.class)))
                .willReturn(ApiResponse.success("Created", new PolicyResponse()));

        mockMvc.perform(post("/api/policies")
                .header("X-UserId", "1")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Roles", "ROLE_CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void purchasePolicy_returns201() throws Exception {
        PurchasePolicyRequest request = PurchasePolicyRequest.builder()
                .holderName("Alice")
                .holderEmail("alice@test.com")
                .policyType(com.smartsure.policy.entity.PolicyType.PROPERTY)
                .coverageAmount(new java.math.BigDecimal("5000"))
                .build();

        given(policyService.purchasePolicy(any(PurchasePolicyRequest.class)))
                .willReturn(ApiResponse.success("Purchased", new PolicyResponse()));

        mockMvc.perform(post("/api/policies/purchase")
                .header("X-User-Email", "alice@test.com")
                .header("X-User-Roles", "ROLE_CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllPolicies_returns200() throws Exception {
        given(policyService.getAllPolicies()).willReturn(ApiResponse.success("OK", Collections.emptyList()));
        mockMvc.perform(get("/api/policies")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllPolicies_Customer_Returns200() throws Exception {
        given(policyService.getPoliciesByEmail("john@test.com")).willReturn(ApiResponse.success("OK", Collections.emptyList()));
        mockMvc.perform(get("/api/policies")
                        .header("X-User-Email", "john@test.com")
                        .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    void getPolicyById_returns200() throws Exception {
        given(policyService.getPolicyById(1L)).willReturn(ApiResponse.success("OK", new PolicyResponse()));

        mockMvc.perform(get("/api/policies/1")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getPolicyByNumber_returns200() throws Exception {
        given(policyService.getPolicyByNumber("POL-123")).willReturn(ApiResponse.success("OK", new PolicyResponse()));

        mockMvc.perform(get("/api/policies/number/POL-123")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getPoliciesByStatus_returns200() throws Exception {
        given(policyService.getPoliciesByStatus(PolicyStatus.ACTIVE))
                .willReturn(ApiResponse.success("OK", Collections.emptyList()));

        mockMvc.perform(get("/api/policies/status/ACTIVE")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void updatePolicyStatus_returns200() throws Exception {
        given(policyService.updatePolicyStatus(anyLong(), any(PolicyStatus.class)))
                .willReturn(ApiResponse.success("Updated", new PolicyResponse()));

        mockMvc.perform(put("/api/policies/1/status")
                .header("X-User-Roles", "ROLE_ADMIN")
                .param("status", "INACTIVE"))
                .andExpect(status().isOk());
    }

    @Test
    void deletePolicy_returns200() throws Exception {
        given(policyService.deletePolicy(1L)).willReturn(ApiResponse.success("Deleted", null));

        mockMvc.perform(delete("/api/policies/1")
                        .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }
}
