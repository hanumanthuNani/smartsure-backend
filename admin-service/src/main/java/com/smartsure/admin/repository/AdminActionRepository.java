package com.smartsure.admin.repository;

import com.smartsure.admin.entity.AdminAction;
import com.smartsure.admin.entity.AdminActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, Long> {
    List<AdminAction> findByAdminId(Long adminId);
    List<AdminAction> findByActionType(AdminActionType type);
    List<AdminAction> findByTargetId(Long targetId);
}
