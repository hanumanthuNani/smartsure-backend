package com.smartsure.claims.messaging;

import com.smartsure.claims.dto.ClaimResponse;
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
    private ClaimResponse claim;
    private LocalDateTime occurredAt;
}
