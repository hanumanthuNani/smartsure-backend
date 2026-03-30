package com.smartsure.policy.service;

import com.smartsure.policy.entity.PolicyType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PremiumCalculatorTest {

    private final PremiumCalculator calculator = new PremiumCalculator();

    @Test
    void testCalculate_Health() {
        BigDecimal result = calculator.calculate(PolicyType.HEALTH, new BigDecimal("1000"));
        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void testCalculate_Life() {
        BigDecimal result = calculator.calculate(PolicyType.LIFE, new BigDecimal("1000"));
        assertEquals(new BigDecimal("15.00"), result);
    }

    @Test
    void testCalculate_Vehicle() {
        BigDecimal result = calculator.calculate(PolicyType.VEHICLE, new BigDecimal("1000"));
        assertEquals(new BigDecimal("30.00"), result);
    }

    @Test
    void testCalculate_Property() {
        BigDecimal result = calculator.calculate(PolicyType.PROPERTY, new BigDecimal("1000"));
        assertEquals(new BigDecimal("25.00"), result);
    }
}
