package com.smartsure.admin.messaging;

import com.smartsure.admin.repository.AdminActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class ClaimEventConsumerTest {

    @Mock
    private AdminActionRepository adminActionRepository;

    @InjectMocks
    private ClaimEventConsumer claimEventConsumer;

    @Test
    void receiveClaimEvent_Success() {
        ClaimEvent event = ClaimEvent.builder()
                .claimId(1L)
                .claimNumber("C123")
                .status("CREATED")
                .eventType("CLAIM_CREATED")
                .occurredAt(LocalDateTime.now())
                .build();

        assertDoesNotThrow(() -> claimEventConsumer.receiveClaimEvent(event));
    }
}
