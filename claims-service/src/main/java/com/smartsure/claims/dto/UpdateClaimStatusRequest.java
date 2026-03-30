package com.smartsure.claims.dto;

import com.smartsure.claims.entity.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClaimStatusRequest {

    @NotNull(message = "Status is required")
    private ClaimStatus status;

    private String rejectionReason;
}
