package com.smartsure.policy.mapper;

import com.smartsure.policy.dto.CreatePolicyRequest;
import com.smartsure.policy.dto.PolicyResponse;
import com.smartsure.policy.dto.PurchasePolicyRequest;
import com.smartsure.policy.entity.Policy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PolicyMapper {

    Policy toPolicy(CreatePolicyRequest request);
    Policy toPolicy(PurchasePolicyRequest request);
    PolicyResponse toResponse(Policy policy);
}
