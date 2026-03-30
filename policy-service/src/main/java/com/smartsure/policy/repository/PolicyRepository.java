package com.smartsure.policy.repository;

import com.smartsure.policy.entity.Policy;
import com.smartsure.policy.entity.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByHolderEmail(String holderEmail);
    List<Policy> findByStatus(PolicyStatus status);
    List<Policy> findByCreatedBy(Long createdBy);
}
