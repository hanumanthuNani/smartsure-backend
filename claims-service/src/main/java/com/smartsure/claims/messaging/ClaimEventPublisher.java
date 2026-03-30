package com.smartsure.claims.messaging;

import com.smartsure.claims.dto.ClaimResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:claims.exchange}")
    private String exchange;

    public void publishClaimEvent(ClaimResponse claim, String eventType) {
        String routingKey = "claim." + eventType.toLowerCase();
        
        ClaimEvent event = ClaimEvent.builder()
                .eventType(eventType)
                .claim(claim)
                .occurredAt(LocalDateTime.now())
                .build();
                
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Published claim event '{}' with routing key '{}' for claim ID: {}", eventType, routingKey, claim.getId());
    }
}
