package com.smartsure.claims.mapper;

import com.smartsure.claims.dto.DocumentResponse;
import com.smartsure.claims.entity.Claim;
import com.smartsure.claims.entity.ClaimDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {

    private DocumentMapper documentMapper;

    @BeforeEach
    void setUp() {
        documentMapper = new DocumentMapperImpl();
    }

    @Test
    void testToResponse() {
        Claim claim = Claim.builder().id(10L).build();
        ClaimDocument doc = ClaimDocument.builder()
                .id(1L)
                .claim(claim)
                .fileName("test.pdf")
                .build();
        
        DocumentResponse response = documentMapper.toResponse(doc);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getClaimId());
        assertEquals("test.pdf", response.getFileName());
    }
}
