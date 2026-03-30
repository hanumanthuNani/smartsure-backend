package com.smartsure.admin.messaging;

import com.smartsure.admin.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:email.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key:email.routing.key}")
    private String routingKey;

    public void sendEmailNotification(String to, String subject, String body) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .build();

        rabbitTemplate.convertAndSend(exchange, routingKey, emailRequest);
        log.info("Sent email notification event to {} with subject: {}", to, subject);
    }
}
