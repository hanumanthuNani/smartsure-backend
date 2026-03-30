package com.smartsure.policy.dto;

import com.smartsure.policy.entity.PolicyStatus;
import com.smartsure.policy.entity.PolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private PolicyType policyType;
    private String holderName;
    private String holderEmail;
    private BigDecimal premium;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private PolicyStatus status;
    private LocalDateTime createdAt;
}
