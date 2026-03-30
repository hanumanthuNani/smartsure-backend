package com.smartsure.claims.mapper;

import com.smartsure.claims.dto.DocumentResponse;
import com.smartsure.claims.entity.ClaimDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(source = "claim.id", target = "claimId")
    DocumentResponse toResponse(ClaimDocument document);
}
