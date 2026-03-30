package com.smartsure.admin.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimEvent {
    private String eventType;
    private Long claimId;
    private String claimNumber;
    private String status;
    private LocalDateTime occurredAt;
}
