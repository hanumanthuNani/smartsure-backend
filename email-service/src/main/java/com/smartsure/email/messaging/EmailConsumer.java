package com.smartsure.email.messaging;

import com.smartsure.email.dto.EmailRequest;
import com.smartsure.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = "${rabbitmq.queue:email.queue}")
    public void consumeEmailMessage(EmailRequest request) {
        log.info("Received email request for: {} with subject: {}", request.getTo(), request.getSubject());
        emailService.sendSimpleEmail(request);
    }

    @RabbitListener(queues = "policy.queue")
    public void consumePolicyPurchaseEvent(com.smartsure.email.dto.PolicyResponse response) {
        log.info("Received policy purchase event for holder: {}", response.getHolderEmail());
        emailService.sendPolicyPurchaseEmail(response);
    }
}
