package com.smartsure.email.service;

import com.smartsure.email.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendSimpleEmail(EmailRequest request) {
        log.info("Sending email to: {} with subject: {}", request.getTo(), request.getSubject());
        sendEmail(request.getTo(), request.getSubject(), request.getBody());
    }

    @Override
    public void sendPolicyPurchaseEmail(com.smartsure.email.dto.PolicyResponse response) {
        log.info("Sending policy purchase confirmation to: {}", response.getHolderEmail());
        String subject = "Your SmartSure Protection is Active! 🛡️";
        String body = "Hello " + response.getHolderName() + ",\n\n" +
                "Congratulations! Your " + response.getPolicyType() + " policy is now ACTIVE.\n\n" +
                "Policy Details:\n" +
                "- Policy Number: " + response.getPolicyNumber() + "\n" +
                "- Coverage Amount: $" + response.getCoverageAmount() + "\n" +
                "- Annual Premium: $" + response.getPremium() + "\n" +
                "- Effective Date: " + response.getStartDate() + "\n" +
                "- Expiry Date: " + response.getEndDate() + "\n\n" +
                "Your protection is our priority. You can view your policy documents anytime through the user portal.\n\n" +
                "Thank you for trusting SmartSure.\n\n" +
                "Stay protected,\n" +
                "The SmartSure Team";

        sendEmail(response.getHolderEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("SmartSure <noreply@smartsure.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
        }
    }
}
