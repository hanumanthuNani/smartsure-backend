package com.smartsure.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClaimStatusRequest {
    private ClaimStatus status;
    private String rejectionReason;
}
