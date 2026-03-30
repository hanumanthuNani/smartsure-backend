package com.smartsure.admin.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClaimEventConsumer {

    @RabbitListener(queues = "claims.queue")
    public void receiveClaimEvent(ClaimEvent event) {
        log.info("Received claim event: {} for claim: {}", event.getEventType(), event.getClaimId());
    }
}
