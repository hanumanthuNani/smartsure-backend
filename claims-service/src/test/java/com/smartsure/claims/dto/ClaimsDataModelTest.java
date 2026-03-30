package com.smartsure.claims.dto;

import com.smartsure.claims.entity.*;
import com.smartsure.claims.messaging.ClaimEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ClaimsDataModelTest {

    @Test
    void testEntities() {
        Claim claim = Claim.builder()
                .id(1L)
                .claimNumber("CLM123")
                .policyNumber("POL123")
                .policyId(1L)
                .claimantName("Name")
                .claimantEmail("test@test.com")
                .claimType(ClaimType.MEDICAL)
                .description("Desc")
                .claimAmount(BigDecimal.TEN)
                .status(ClaimStatus.SUBMITTED)
                .rejectionReason("Reason")
                .submittedAt(LocalDateTime.now())
                .reviewedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .build();

        assertEquals(1L, claim.getId());
        assertEquals("CLM123", claim.getClaimNumber());
        assertEquals("POL123", claim.getPolicyNumber());
        assertEquals(1L, claim.getPolicyId());
        assertEquals("Name", claim.getClaimantName());
        assertEquals("test@test.com", claim.getClaimantEmail());
        assertEquals(ClaimType.MEDICAL, claim.getClaimType());
        assertEquals("Desc", claim.getDescription());
        assertEquals(BigDecimal.TEN, claim.getClaimAmount());
        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
        assertEquals("Reason", claim.getRejectionReason());
        assertNotNull(claim.getSubmittedAt());
        assertNotNull(claim.getReviewedAt());
        assertNotNull(claim.getCreatedAt());
        assertEquals(1L, claim.getCreatedBy());

        claim.prePersist(); // Test prePersist branch
        
        ClaimDocument doc = ClaimDocument.builder()
                .id(1L)
                .claim(claim)
                .fileName("file.txt")
                .fileType("text/plain")
                .filePath("/path")
                .fileSize(100L)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(1L)
                .build();

        assertEquals(1L, doc.getId());
        assertEquals(claim, doc.getClaim());
        assertEquals("file.txt", doc.getFileName());
        assertEquals("text/plain", doc.getFileType());
        assertEquals("/path", doc.getFilePath());
        assertEquals(100L, doc.getFileSize());
        assertNotNull(doc.getUploadedAt());
        assertEquals(1L, doc.getUploadedBy());
    }

    @Test
    void testDtos() {
        CreateClaimRequest createReq = CreateClaimRequest.builder()
                .policyNumber("POL123")
                .policyId(1L)
                .claimantName("Name")
                .claimantEmail("test@test.com")
                .claimType(ClaimType.ACCIDENT)
                .description("Desc")
                .claimAmount(BigDecimal.ONE)
                .build();
        
        assertEquals("POL123", createReq.getPolicyNumber());

        UpdateClaimStatusRequest updateReq = UpdateClaimStatusRequest.builder()
                .status(ClaimStatus.APPROVED)
                .rejectionReason("None")
                .build();
        
        assertEquals(ClaimStatus.APPROVED, updateReq.getStatus());

        ClaimResponse claimRes = ClaimResponse.builder()
                .id(1L)
                .claimNumber("C")
                .build();
        assertEquals(1L, claimRes.getId());

        DocumentResponse docRes = DocumentResponse.builder()
                .id(1L)
                .fileName("F")
                .build();
        assertEquals(1L, docRes.getId());

        PolicyResponse polRes = PolicyResponse.builder()
                .id(1L)
                .policyNumber("P")
                .status("S")
                .holderEmail("E")
                .build();
        assertEquals(1L, polRes.getId());

        ApiResponse<String> apiRes = ApiResponse.success("Msg", "Data");
        assertTrue(apiRes.isSuccess());
        assertEquals("Msg", apiRes.getMessage());
        assertEquals("Data", apiRes.getData());

        ApiResponse<Void> apiError = ApiResponse.error("Err");
        assertFalse(apiError.isSuccess());
    }

    @Test
    void testMessaging() {
        ClaimEvent event = ClaimEvent.builder()
                .eventType("TYPE")
                .claim(new ClaimResponse())
                .occurredAt(LocalDateTime.now())
                .build();
        
        assertEquals("TYPE", event.getEventType());
        assertNotNull(event.getClaim());
        assertNotNull(event.getOccurredAt());
    }
}
