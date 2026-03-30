package com.smartsure.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActionType actionType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String targetType;

    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
