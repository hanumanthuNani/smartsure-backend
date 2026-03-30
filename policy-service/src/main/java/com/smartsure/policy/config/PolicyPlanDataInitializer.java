package com.smartsure.policy.config;

import com.smartsure.policy.entity.PolicyPlan;
import com.smartsure.policy.entity.PolicyType;
import com.smartsure.policy.repository.PolicyPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyPlanDataInitializer implements CommandLineRunner {

    private final PolicyPlanRepository policyPlanRepository;

    @Override
    public void run(String... args) {
        if (policyPlanRepository.count() == 0) {
            log.info("Initializing PolicyBazaar-style Gold/Silver/Platinum plans...");
            
            // Health Plans
            createPlan("Health Silver", PolicyType.HEALTH, 200000, 500, List.of("Cashless Hospitalization", "Ambulance Cover"));
            createPlan("Health Gold", PolicyType.HEALTH, 500000, 1200, List.of("Cashless Hospitalization", "OPD Cover", "Maternity Benefit"));
            createPlan("Health Platinum", PolicyType.HEALTH, 1000000, 2500, List.of("Cashless Hospitalization", "Global Cover", "Critical Illness"));

            // Life Plans
            createPlan("Life Silver", PolicyType.LIFE, 1000000, 300, List.of("Basic Death Benefit"));
            createPlan("Life Gold", PolicyType.LIFE, 5000000, 800, List.of("Death Benefit", "Accidental Rider"));
            createPlan("Life Platinum", PolicyType.LIFE, 10000000, 1500, List.of("Death Benefit", "Accidental + Disability Rider", "Premium Waiver"));

            // Vehicle Plans
            createPlan("Vehicle Silver", PolicyType.VEHICLE, 300000, 400, List.of("Third Party Liability"));
            createPlan("Vehicle Gold", PolicyType.VEHICLE, 700000, 900, List.of("Third Party Liability", "Accidental Damage", "Zero Depreciation"));
            createPlan("Vehicle Platinum", PolicyType.VEHICLE, 1500000, 2000, List.of("Full Comprehensive", "Zero Depreciation", "Engine Protection", "Roadside Assistance"));

            log.info("Successfully pre-loaded {} policy plans.", policyPlanRepository.count());
        }
    }

    private void createPlan(String name, PolicyType type, double coverage, double premium, List<String> benefits) {
        PolicyPlan plan = PolicyPlan.builder()
                .planName(name)
                .policyType(type)
                .coverageAmount(BigDecimal.valueOf(coverage))
                .monthlyPremium(BigDecimal.valueOf(premium))
                .benefits(benefits)
                .description("Professional " + name + " for " + type + " coverage.")
                .build();
        policyPlanRepository.save(plan);
    }
}
