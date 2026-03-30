package com.smartsure.policy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType policyType;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private String holderEmail;

    @Column(nullable = false)
    private BigDecimal premium;

    @Column(nullable = false)
    private BigDecimal coverageAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PolicyStatus status = PolicyStatus.INACTIVE;

    private Long createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (policyNumber == null) {
            policyNumber = "POL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
