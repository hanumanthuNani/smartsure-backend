package com.smartsure.claims.messaging;

import com.smartsure.claims.dto.ClaimResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClaimEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ClaimEventPublisher claimEventPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(claimEventPublisher, "exchange", "test-exchange");
    }

    @Test
    void publishClaimEvent_SendsMessage() {
        ClaimResponse claim = new ClaimResponse();
        claim.setId(1L);
        
        claimEventPublisher.publishClaimEvent(claim, "CREATED");
        
        verify(rabbitTemplate).convertAndSend(eq("test-exchange"), eq("claim.created"), any(ClaimEvent.class));
    }
}
