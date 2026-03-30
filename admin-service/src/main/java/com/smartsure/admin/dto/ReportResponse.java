package com.smartsure.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long totalPolicies;
    private Long activePolicies;
    private Long totalClaims;
    private Long pendingClaims;
    private Long approvedClaims;
    private Long rejectedClaims;
    private Long totalAdminActions;
}
