package com.smartsure.claims.service;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.DocumentResponse;
import com.smartsure.claims.entity.Claim;
import com.smartsure.claims.entity.ClaimDocument;
import com.smartsure.claims.exception.ResourceNotFoundException;
import com.smartsure.claims.mapper.DocumentMapper;
import com.smartsure.claims.repository.ClaimDocumentRepository;
import com.smartsure.claims.repository.ClaimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private ClaimDocumentRepository claimDocumentRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void uploadDocument_Success() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(claimRepository.findById(1L)).thenReturn(Optional.of(new Claim()));
        when(fileStorageService.storeFile(any(), anyLong())).thenReturn("/path");
        when(claimDocumentRepository.save(any())).thenReturn(new ClaimDocument());
        when(documentMapper.toResponse(any())).thenReturn(new DocumentResponse());

        ApiResponse<DocumentResponse> response = documentService.uploadDocument(1L, file, 1L);

        assertTrue(response.isSuccess());
        verify(claimDocumentRepository).save(any());
    }

    @Test
    void uploadDocument_ClaimNotFound() {
        MultipartFile file = mock(MultipartFile.class);
        when(claimRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentService.uploadDocument(1L, file, 1L));
    }

    @Test
    void getDocumentsByClaim_Success() {
        when(claimRepository.existsById(1L)).thenReturn(true);
        when(claimDocumentRepository.findByClaimId(1L)).thenReturn(Collections.singletonList(new ClaimDocument()));
        when(documentMapper.toResponse(any())).thenReturn(new DocumentResponse());

        ApiResponse<List<DocumentResponse>> response = documentService.getDocumentsByClaim(1L);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
    }

    @Test
    void getDocumentsByClaim_NotFound() {
        when(claimRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> documentService.getDocumentsByClaim(1L));
    }
}
