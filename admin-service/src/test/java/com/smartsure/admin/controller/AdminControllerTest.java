package com.smartsure.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsure.admin.dto.*;
import com.smartsure.admin.service.AdminService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void approveClaim_returns200() throws Exception {
        ClaimActionRequest request = new ClaimActionRequest(1L, "APPROVE", "Reason");
        when(adminService.approveClaim(eq(1L), eq("Reason"), any())).thenReturn(ApiResponse.success("Success", null));

        mockMvc.perform(post("/api/admin/claims/approve")
                .header("X-User-Roles", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void rejectClaim_returns200() throws Exception {
        ClaimActionRequest request = new ClaimActionRequest(1L, "REJECT", "Reason");
        when(adminService.rejectClaim(eq(1L), eq("Reason"), any())).thenReturn(ApiResponse.success("Success", null));

        mockMvc.perform(post("/api/admin/claims/reject")
                .header("X-User-Roles", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void activatePolicy_returns200() throws Exception {
        PolicyActionRequest request = new PolicyActionRequest(1L, "ACTIVATE", "Reason");
        when(adminService.activatePolicy(eq(1L), eq("Reason"), any())).thenReturn(ApiResponse.success("Success", null));

        mockMvc.perform(post("/api/admin/policies/activate")
                .header("X-User-Roles", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deactivatePolicy_returns200() throws Exception {
        PolicyActionRequest request = new PolicyActionRequest(1L, "DEACTIVATE", "Reason");
        when(adminService.deactivatePolicy(eq(1L), eq("Reason"), any())).thenReturn(ApiResponse.success("Success", null));

        mockMvc.perform(post("/api/admin/policies/deactivate")
                .header("X-User-Roles", "ROLE_ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllActions_returnsList() throws Exception {
        when(adminService.getAllActions()).thenReturn(ApiResponse.success("Success", Collections.emptyList()));

        mockMvc.perform(get("/api/admin/actions")
                .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getActionsByAdmin_returnsList() throws Exception {
        when(adminService.getActionsByAdmin(1L)).thenReturn(ApiResponse.success("Success", Collections.emptyList()));

        mockMvc.perform(get("/api/admin/actions/admin/1")
                .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getActionsByTarget_returnsList() throws Exception {
        when(adminService.getActionsByTarget(1L)).thenReturn(ApiResponse.success("Success", Collections.emptyList()));

        mockMvc.perform(get("/api/admin/actions/target/1")
                .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void generateReport_returnsData() throws Exception {
        ReportResponse report = ReportResponse.builder().totalPolicies(5L).build();
        when(adminService.generateReport()).thenReturn(ApiResponse.success("Success", report));

        mockMvc.perform(get("/api/admin/reports")
                .header("X-User-Roles", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalPolicies").value(5));
    }

    @Test
    void accessDenied_withoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/reports")
                .header("X-User-Roles", "ROLE_CUSTOMER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Access Denied: You Are Not Authorized"));
    }
}
