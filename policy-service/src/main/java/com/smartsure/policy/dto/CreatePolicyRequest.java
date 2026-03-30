package com.smartsure.policy.dto;

import com.smartsure.policy.entity.PolicyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreatePolicyRequest {

    @NotBlank(message = "Holder name is required")
    private String holderName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Holder email is required")
    private String holderEmail;

    @NotNull(message = "Policy type is required")
    private PolicyType policyType;

    @NotNull(message = "Coverage amount is required")
    @Positive(message = "Coverage amount must be positive")
    private BigDecimal coverageAmount;

    @NotNull(message = "Premium is required")
    @Positive(message = "Premium must be positive")
    private BigDecimal premium;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long createdBy;
}
