package com.smartsure.claims.repository;

import com.smartsure.claims.entity.Claim;
import com.smartsure.claims.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);
    List<Claim> findByPolicyNumber(String policyNumber);
    List<Claim> findByPolicyId(Long policyId);
    List<Claim> findByStatus(ClaimStatus status);
    List<Claim> findByClaimantEmail(String claimantEmail);
    List<Claim> findByCreatedBy(Long createdBy);
}
