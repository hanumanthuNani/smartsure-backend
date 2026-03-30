package com.smartsure.policy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "policy_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String planName; // Gold, Silver, Platinum

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType policyType;

    @Column(nullable = false)
    private BigDecimal coverageAmount;

    @Column(nullable = false)
    private BigDecimal monthlyPremium;

    @ElementCollection
    @CollectionTable(name = "plan_benefits", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "benefit")
    private List<String> benefits;

    private String description;
}
