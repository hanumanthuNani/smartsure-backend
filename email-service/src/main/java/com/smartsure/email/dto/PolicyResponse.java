package com.smartsure.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {
    private Long id;
    private String policyNumber;
    private String policyType;
    private String holderName;
    private String holderEmail;
    private BigDecimal premium;
    private BigDecimal coverageAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
