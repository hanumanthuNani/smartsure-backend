package com.smartsure.admin.mapper;

import com.smartsure.admin.dto.AdminActionResponse;
import com.smartsure.admin.entity.AdminAction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminMapper {
    AdminActionResponse toResponse(AdminAction entity);
}
