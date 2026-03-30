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
public class PolicyActionRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotBlank(message = "Action is required")
    private String action; // ACTIVATE or DEACTIVATE

    private String reason;
}
