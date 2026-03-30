package com.smartsure.admin.dto;

import com.smartsure.admin.entity.AdminActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionResponse {
    private Long id;
    private Long adminId;
    private AdminActionType actionType;
    private Long targetId;
    private String targetType;
    private String reason;
    private LocalDateTime createdAt;
}
