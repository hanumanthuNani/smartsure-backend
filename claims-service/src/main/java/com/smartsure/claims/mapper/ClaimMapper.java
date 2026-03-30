package com.smartsure.claims.mapper;

import com.smartsure.claims.dto.ClaimResponse;
import com.smartsure.claims.dto.CreateClaimRequest;
import com.smartsure.claims.entity.Claim;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClaimMapper {

    Claim toEntity(CreateClaimRequest request);

    ClaimResponse toResponse(Claim claim);
}
