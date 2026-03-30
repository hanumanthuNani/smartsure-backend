package com.smartsure.claims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String claimNumber;

    @Column(nullable = false)
    private String policyNumber;

    @Column(nullable = false)
    private Long policyId;

    @Column(nullable = false)
    private String claimantName;

    @Column(nullable = false)
    private String claimantEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimType claimType;

    private String description;

    @Column(nullable = false)
    private BigDecimal claimAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus status;

    private String rejectionReason;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Long createdBy;

    @PrePersist
    public void prePersist() {
        if (this.claimNumber == null) {
            this.claimNumber = "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.status == null) {
            this.status = ClaimStatus.DRAFT;
        }
    }
}
