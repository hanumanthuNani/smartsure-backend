package com.smartsure.auth.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:email.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key:email.routing.key}")
    private String routingKey;

    public void publishEmailEvent(EmailRequest request) {
        log.info("Publishing email event for: {}", request.getTo());
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, request);
            log.info("Successfully published email event to RabbitMQ");
        } catch (Exception e) {
            log.error("Failed to publish email event: {}", e.getMessage());
        }
    }
}
