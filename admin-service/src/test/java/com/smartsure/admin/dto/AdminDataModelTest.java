package com.smartsure.admin.dto;

import com.smartsure.admin.entity.AdminAction;
import com.smartsure.admin.entity.AdminActionType;
import com.smartsure.admin.messaging.ClaimEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminDataModelTest {

    @Test
    void testDtoAndEntities() {
        // ApiResponse
        ApiResponse<String> apiResponse = new ApiResponse<>(true, "message", "data");
        assertTrue(apiResponse.isSuccess());
        assertEquals("message", apiResponse.getMessage());
        assertEquals("data", apiResponse.getData());
        ApiResponse<String> apiResponse2 = new ApiResponse<>();
        apiResponse2.setSuccess(false);
        apiResponse2.setMessage("msg");
        apiResponse2.setData(null);
        assertFalse(apiResponse2.isSuccess());

        // AdminActionResponse
        AdminActionResponse adminActionResponse = AdminActionResponse.builder()
                .id(1L)
                .adminId(2L)
                .actionType(AdminActionType.APPROVE_CLAIM)
                .targetId(3L)
                .targetType("CLAIM")
                .reason("reason")
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals(1L, adminActionResponse.getId());
        assertEquals(2L, adminActionResponse.getAdminId());
        assertEquals("reason", adminActionResponse.getReason());
        
        AdminActionResponse a2 = new AdminActionResponse();
        a2.setId(1L);
        a2.setAdminId(1L);
        a2.setActionType(AdminActionType.APPROVE_CLAIM);
        a2.setTargetId(1L);
        a2.setTargetType("T");
        a2.setReason("R");
        a2.setCreatedAt(LocalDateTime.now());
        assertEquals("T", a2.getTargetType());

        // ClaimActionRequest
        ClaimActionRequest claimActionRequest = new ClaimActionRequest(1L, "APPROVE", "reason");
        assertEquals(1L, claimActionRequest.getClaimId());
        assertEquals("APPROVE", claimActionRequest.getAction());
        ClaimActionRequest c2 = new ClaimActionRequest();
        c2.setClaimId(1L);
        c2.setAction("A");
        c2.setReason("R");

        // ClaimResponse
        ClaimResponse claimResponse = ClaimResponse.builder()
                .id(1L)
                .claimNumber("C123")
                .policyNumber("P123")
                .status("OPEN")
                .claimantName("John")
                .claimantEmail("john@test.com")
                .claimAmount(BigDecimal.TEN)
                .build();
        assertEquals("C123", claimResponse.getClaimNumber());
        ClaimResponse cr2 = new ClaimResponse(1L, "n", "p", "s", "n", "e", BigDecimal.ZERO);
        assertEquals("n", cr2.getClaimNumber());

        // PolicyActionRequest
        PolicyActionRequest policyActionRequest = new PolicyActionRequest(1L, "ACTIVATE", "reason");
        assertEquals(1L, policyActionRequest.getPolicyId());
        PolicyActionRequest p2 = new PolicyActionRequest();
        p2.setPolicyId(1L);
        p2.setAction("A");
        p2.setReason("R");

        // PolicyResponse
        PolicyResponse policyResponse = PolicyResponse.builder()
                .id(1L)
                .policyNumber("P123")
                .status("ACTIVE")
                .holderName("Holder")
                .holderEmail("holder@test.com")
                .build();
        assertEquals("P123", policyResponse.getPolicyNumber());
        PolicyResponse pr2 = new PolicyResponse(1L, "n", "s", "h", "e");
        assertEquals("n", pr2.getPolicyNumber());

        // ReportResponse
        ReportResponse reportResponse = ReportResponse.builder()
                .totalPolicies(10L)
                .activePolicies(5L)
                .totalClaims(20L)
                .pendingClaims(7L)
                .approvedClaims(8L)
                .rejectedClaims(5L)
                .totalAdminActions(15L)
                .build();
        assertEquals(10L, reportResponse.getTotalPolicies());
        ReportResponse rr2 = new ReportResponse(1L, 1L, 1L, 1L, 1L, 1L, 1L);
        assertEquals(1L, rr2.getTotalClaims());

        // UpdateClaimStatusRequest
        UpdateClaimStatusRequest updateClaimStatusRequest = new UpdateClaimStatusRequest(ClaimStatus.APPROVED, "reason");
        assertEquals(ClaimStatus.APPROVED, updateClaimStatusRequest.getStatus());
        UpdateClaimStatusRequest u2 = new UpdateClaimStatusRequest();
        u2.setStatus(ClaimStatus.REJECTED);
        u2.setRejectionReason("R");

        // AdminAction Entity
        AdminAction adminAction = AdminAction.builder()
                .id(1L)
                .adminId(2L)
                .actionType(AdminActionType.APPROVE_CLAIM)
                .targetId(3L)
                .targetType("CLAIM")
                .reason("reason")
                .createdAt(LocalDateTime.now())
                .build();
        assertEquals(1L, adminAction.getId());
        AdminAction aa2 = new AdminAction();
        aa2.setId(1L);
        aa2.setAdminId(1L);
        aa2.setActionType(AdminActionType.APPROVE_CLAIM);
        aa2.setTargetId(1L);
        aa2.setTargetType("T");
        aa2.setReason("R");
        aa2.setCreatedAt(LocalDateTime.now());
        
        // ClaimEvent
        ClaimEvent event = ClaimEvent.builder()
                .claimId(1L)
                .claimNumber("C1")
                .status("S")
                .eventType("E")
                .occurredAt(LocalDateTime.now())
                .build();
        assertEquals(1L, event.getClaimId());
        ClaimEvent e2 = new ClaimEvent("e", 1L, "n", "s", LocalDateTime.now());
        assertEquals("e", e2.getEventType());
    }
}
