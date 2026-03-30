package com.smartsure.policy.mapper;

import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.entity.Policy;
import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.entity.PolicyType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PolicyMapperTest {

    private final PolicyMapper mapper = Mappers.getMapper(PolicyMapper.class);

    @Test
    void toPolicy() {
        CreatePolicyRequest request = CreatePolicyRequest.builder()
                .holderName("John")
                .holderEmail("john@test.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("1000"))
                .premium(new BigDecimal("20"))
                .build();

        Policy entity = mapper.toPolicy(request);

        assertNotNull(entity);
        assertEquals("John", entity.getHolderName());
        assertEquals("john@test.com", entity.getHolderEmail());
        assertEquals(PolicyType.HEALTH, entity.getPolicyType());
    }

    @Test
    void toResponse() {
        Policy policy = Policy.builder()
                .id(1L)
                .policyNumber("POL-123")
                .holderName("Jane")
                .status(PolicyStatus.ACTIVE)
                .policyType(PolicyType.LIFE)
                .build();

        PolicyResponse response = mapper.toResponse(policy);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("POL-123", response.getPolicyNumber());
        assertEquals(PolicyStatus.ACTIVE, response.getStatus());
    }
}
