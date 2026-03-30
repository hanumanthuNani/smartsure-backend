package com.smartsure.email.service;

import com.smartsure.email.dto.EmailRequest;
import com.smartsure.email.dto.PolicyResponse;

public interface EmailService {
    void sendSimpleEmail(EmailRequest request);
    void sendPolicyPurchaseEmail(PolicyResponse response);
}
