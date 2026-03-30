package com.smartsure.policy.repository;

import com.smartsure.policy.entity.PolicyPlan;
import com.smartsure.policy.entity.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyPlanRepository extends JpaRepository<PolicyPlan, Long> {
    List<PolicyPlan> findByPolicyType(PolicyType policyType);
}
