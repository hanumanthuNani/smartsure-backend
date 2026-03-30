package com.smartsure.claims.dto;

import com.smartsure.claims.entity.ClaimType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequest {

    @NotBlank(message = "Policy number is required")
    private String policyNumber;

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotBlank(message = "Claimant name is required")
    private String claimantName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Claimant email is required")
    private String claimantEmail;

    @NotNull(message = "Claim type is required")
    private ClaimType claimType;

    private String description;

    @NotNull(message = "Claim amount is required")
    @Positive(message = "Claim amount must be positive")
    private BigDecimal claimAmount;
}
