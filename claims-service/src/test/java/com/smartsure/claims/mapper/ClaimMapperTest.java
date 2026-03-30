package com.smartsure.claims.mapper;

import com.smartsure.claims.dto.ClaimResponse;
import com.smartsure.claims.dto.CreateClaimRequest;
import com.smartsure.claims.entity.Claim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ClaimMapperTest {

    private ClaimMapper claimMapper;

    @BeforeEach
    void setUp() {
        claimMapper = new ClaimMapperImpl();
    }

    @Test
    void testToEntity() {
        CreateClaimRequest request = CreateClaimRequest.builder()
                .policyNumber("POL123")
                .claimAmount(BigDecimal.TEN)
                .build();
        Claim entity = claimMapper.toEntity(request);
        assertNotNull(entity);
        assertEquals("POL123", entity.getPolicyNumber());
        assertEquals(BigDecimal.TEN, entity.getClaimAmount());
    }

    @Test
    void testToResponse() {
        Claim claim = Claim.builder()
                .id(1L)
                .claimNumber("CLM123")
                .build();
        ClaimResponse response = claimMapper.toResponse(claim);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CLM123", response.getClaimNumber());
    }
}
