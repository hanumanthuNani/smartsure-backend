package com.smartsure.claims.service;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    ApiResponse<DocumentResponse> uploadDocument(Long claimId, MultipartFile file, Long userId);
    ApiResponse<List<DocumentResponse>> getDocumentsByClaim(Long claimId);
}
