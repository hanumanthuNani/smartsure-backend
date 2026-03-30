package com.smartsure.claims.dto;

import com.smartsure.claims.entity.ClaimStatus;
import com.smartsure.claims.entity.ClaimType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {
    private Long id;
    private String claimNumber;
    private String policyNumber;
    private Long policyId;
    private String claimantName;
    private String claimantEmail;
    private ClaimType claimType;
    private String description;
    private BigDecimal claimAmount;
    private ClaimStatus status;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private Long createdBy;
}
