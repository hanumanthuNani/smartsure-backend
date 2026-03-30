package com.smartsure.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FullClaimResponse {
    private ClaimResponse claim;
    private List<DocumentResponse> documents;
}
