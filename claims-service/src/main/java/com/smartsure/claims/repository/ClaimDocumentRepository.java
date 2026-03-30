package com.smartsure.claims.repository;

import com.smartsure.claims.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {
    List<ClaimDocument> findByClaimId(Long claimId);
    boolean existsByClaimIdAndFileName(Long claimId, String fileName);
}
