package com.smartsure.policy.dto;

import com.smartsure.policy.entity.Policy;
import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.entity.PolicyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PolicyDataModelTest {

    @Test
    void testApiResponse() {
        ApiResponse<String> response1 = new ApiResponse<>(true, "Success", "Data");
        ApiResponse<String> response2 = new ApiResponse<>(true, "Success", "Data");
        ApiResponse<String> response3 = new ApiResponse<>(false, "Error", null);

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertNotEquals(null, response1);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotNull(response1.toString());

        ApiResponse<String> success = ApiResponse.success("OK", "Result");
        assertTrue(success.isSuccess());

        ApiResponse<Object> error = ApiResponse.error("Error");
        assertFalse(error.isSuccess());
        
        ApiResponse<Object> empty = new ApiResponse<>();
        empty.setSuccess(true);
        empty.setMessage("Msg");
        empty.setData(null);
        assertTrue(empty.isSuccess());
        
        ApiResponse<Object> built = ApiResponse.builder()
                .success(true)
                .message("Built")
                .data("Data")
                .build();
        assertEquals("Built", built.getMessage());
        assertNotNull(ApiResponse.builder().toString());
    }

    @Test
    void testCreatePolicyRequest() {
        CreatePolicyRequest request1 = CreatePolicyRequest.builder()
                .holderName("John")
                .holderEmail("john@test.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("1000"))
                .premium(new BigDecimal("20"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .createdBy(1L)
                .build();

        CreatePolicyRequest request2 = CreatePolicyRequest.builder()
                .holderName("John")
                .holderEmail("john@test.com")
                .policyType(PolicyType.HEALTH)
                .coverageAmount(new BigDecimal("1000"))
                .premium(new BigDecimal("20"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .createdBy(1L)
                .build();

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotNull(request1.toString());
        assertNotEquals(null, request1);
        
        CreatePolicyRequest empty = new CreatePolicyRequest();
        empty.setHolderName("Jane");
        empty.setHolderEmail("jane@test.com");
        empty.setPolicyType(PolicyType.LIFE);
        empty.setCoverageAmount(BigDecimal.TEN);
        empty.setPremium(BigDecimal.ONE);
        empty.setStartDate(LocalDate.now());
        empty.setEndDate(LocalDate.now());
        empty.setCreatedBy(1L);
        
        assertEquals("jane@test.com", empty.getHolderEmail());
        assertNotNull(CreatePolicyRequest.builder().toString());
    }

    @Test
    void testPolicyResponse() {
        PolicyResponse response1 = PolicyResponse.builder()
                .id(1L)
                .policyNumber("POL-123")
                .status(PolicyStatus.ACTIVE)
                .policyType(PolicyType.VEHICLE)
                .holderName("Alice")
                .holderEmail("alice@test.com")
                .premium(BigDecimal.TEN)
                .coverageAmount(BigDecimal.TEN)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();

        PolicyResponse response2 = PolicyResponse.builder()
                .id(1L)
                .policyNumber("POL-123")
                .status(PolicyStatus.ACTIVE)
                .policyType(PolicyType.VEHICLE)
                .holderName("Alice")
                .holderEmail("alice@test.com")
                .premium(BigDecimal.TEN)
                .coverageAmount(BigDecimal.TEN)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
        
        // Match timestamps for equals
        response2.setCreatedAt(response1.getCreatedAt());
        response2.setStartDate(response1.getStartDate());
        response2.setEndDate(response1.getEndDate());

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        assertNotNull(response1.toString());
        assertNotEquals(null, response1);
        
        PolicyResponse empty = new PolicyResponse();
        empty.setId(2L);
        empty.setPolicyNumber("P2");
        empty.setPolicyType(PolicyType.HEALTH);
        empty.setHolderName("H");
        empty.setHolderEmail("E");
        empty.setPremium(BigDecimal.ONE);
        empty.setCoverageAmount(BigDecimal.ONE);
        empty.setStartDate(LocalDate.now());
        empty.setEndDate(LocalDate.now());
        empty.setStatus(PolicyStatus.EXPIRED);
        empty.setCreatedAt(LocalDateTime.now());
        
        assertEquals("P2", empty.getPolicyNumber());
        assertNotNull(PolicyResponse.builder().toString());
    }

    @Test
    void testPurchasePolicyRequest() {
        PurchasePolicyRequest request1 = PurchasePolicyRequest.builder()
                .holderName("Alice")
                .holderEmail("alice@test.com")
                .policyType(PolicyType.PROPERTY)
                .coverageAmount(new BigDecimal("5000"))
                .build();

        PurchasePolicyRequest request2 = PurchasePolicyRequest.builder()
                .holderName("Alice")
                .holderEmail("alice@test.com")
                .policyType(PolicyType.PROPERTY)
                .coverageAmount(new BigDecimal("5000"))
                .build();

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotNull(request1.toString());
        
        PurchasePolicyRequest empty = new PurchasePolicyRequest();
        empty.setHolderName("Bob");
        empty.setHolderEmail("bob@test.com");
        empty.setPolicyType(PolicyType.HEALTH);
        empty.setCoverageAmount(BigDecimal.ONE);
        
        assertEquals("bob@test.com", empty.getHolderEmail());
        assertNotNull(PurchasePolicyRequest.builder().toString());
    }

    @Test
    void testPolicyEntity() {
        LocalDateTime now = LocalDateTime.now();
        Policy policy1 = Policy.builder()
                .id(1L)
                .policyNumber("POL-001")
                .holderName("Charlie")
                .holderEmail("charlie@test.com")
                .premium(new BigDecimal("50"))
                .coverageAmount(new BigDecimal("2000"))
                .status(PolicyStatus.ACTIVE)
                .policyType(PolicyType.HEALTH)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .createdBy(1L)
                .createdAt(now)
                .build();

        assertNotNull(policy1.toString());
        
        Policy empty = new Policy();
        empty.setId(2L);
        empty.setPolicyNumber("PN");
        empty.setPolicyType(PolicyType.VEHICLE);
        empty.setHolderName("HN");
        empty.setHolderEmail("HE");
        empty.setPremium(BigDecimal.ZERO);
        empty.setCoverageAmount(BigDecimal.ZERO);
        empty.setStartDate(LocalDate.now());
        empty.setEndDate(LocalDate.now());
        empty.setStatus(PolicyStatus.ACTIVE);
        empty.setCreatedBy(1L);
        empty.setCreatedAt(now);
        
        assertEquals("PN", empty.getPolicyNumber());
        assertEquals(1L, empty.getCreatedBy());
        
        // Test onCreate branch 1: policyNumber is null
        Policy p1 = new Policy();
        p1.onCreate();
        assertNotNull(p1.getPolicyNumber());
        assertTrue(p1.getPolicyNumber().startsWith("POL-"));

        // Test onCreate branch 2: policyNumber is already present
        Policy p2 = new Policy();
        p2.setPolicyNumber("EXISTING");
        p2.onCreate();
        assertEquals("EXISTING", p2.getPolicyNumber());
        
        assertNotNull(Policy.builder().toString());
    }
}
