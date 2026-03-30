package com.smartsure.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private String status;
    private String claimantName;
    private String claimantEmail;
    private BigDecimal claimAmount;
}
