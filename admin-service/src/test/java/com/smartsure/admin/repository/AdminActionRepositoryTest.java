package com.smartsure.admin.repository;

import com.smartsure.admin.entity.AdminAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminActionRepositoryTest {

    @Mock
    private AdminActionRepository adminActionRepository;

    @Test
    void findByAdminId_returnsList() {
        when(adminActionRepository.findByAdminId(1L)).thenReturn(Collections.emptyList());
        List<AdminAction> results = adminActionRepository.findByAdminId(1L);
        assertNotNull(results);
    }

    @Test
    void findByTargetId_returnsList() {
        when(adminActionRepository.findByTargetId(1L)).thenReturn(Collections.emptyList());
        List<AdminAction> results = adminActionRepository.findByTargetId(1L);
        assertNotNull(results);
    }
}
