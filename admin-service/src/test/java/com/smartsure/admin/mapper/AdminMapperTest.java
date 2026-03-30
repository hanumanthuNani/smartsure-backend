package com.smartsure.admin.mapper;

import com.smartsure.admin.dto.AdminActionResponse;
import com.smartsure.admin.entity.AdminAction;
import com.smartsure.admin.entity.AdminActionType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdminMapperTest {

    private final AdminMapper mapper = Mappers.getMapper(AdminMapper.class);

    @Test
    void toResponse_Success() {
        AdminAction action = AdminAction.builder()
                .id(1L)
                .adminId(2L)
                .actionType(AdminActionType.APPROVE_CLAIM)
                .targetId(3L)
                .targetType("CLAIM")
                .reason("reason")
                .createdAt(LocalDateTime.now())
                .build();

        AdminActionResponse response = mapper.toResponse(action);

        assertNotNull(response);
        assertEquals(action.getId(), response.getId());
        assertEquals(action.getAdminId(), response.getAdminId());
        assertEquals(action.getActionType(), response.getActionType());
        assertEquals(action.getTargetId(), response.getTargetId());
        assertEquals(action.getTargetType(), response.getTargetType());
        assertEquals(action.getReason(), response.getReason());
        assertEquals(action.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void toResponse_Null() {
        assertNull(mapper.toResponse(null));
    }
}
