package com.smartsure.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimActionRequest {

    @NotNull(message = "Claim ID is required")
    private Long claimId;

    @NotBlank(message = "Action is required")
    private String action; // APPROVE or REJECT

    private String reason;
}
