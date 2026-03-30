package com.smartsure.policy.service;

import com.smartsure.policy.entity.PolicyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PremiumCalculator {

    public BigDecimal calculate(PolicyType type, BigDecimal coverageAmount) {
        BigDecimal rate = switch (type) {
            case HEALTH -> new BigDecimal("0.02");
            case LIFE -> new BigDecimal("0.015");
            case VEHICLE -> new BigDecimal("0.03");
            case PROPERTY -> new BigDecimal("0.025");
        };
        return coverageAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
