package com.smartsure.claims.service;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.DocumentResponse;
import com.smartsure.claims.entity.Claim;
import com.smartsure.claims.entity.ClaimDocument;
import com.smartsure.claims.exception.ResourceNotFoundException;
import com.smartsure.claims.mapper.DocumentMapper;
import com.smartsure.claims.repository.ClaimDocumentRepository;
import com.smartsure.claims.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final ClaimRepository claimRepository;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final FileStorageService fileStorageService;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public ApiResponse<DocumentResponse> uploadDocument(Long claimId, MultipartFile file, Long userId) {
        log.info("Uploading document for claim ID: {}", claimId);
        
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with ID: " + claimId));

        if (claimDocumentRepository.existsByClaimIdAndFileName(claimId, file.getOriginalFilename())) {
            log.warn("Duplicate file upload attempt for claim ID: {} with filename: {}", claimId, file.getOriginalFilename());
            throw new com.smartsure.claims.exception.ClaimsServiceException("Duplicate file: A document with this name already exists for this claim.");
        }

        String filePath = fileStorageService.storeFile(file, claimId);

        ClaimDocument document = ClaimDocument.builder()
                .claim(claim)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(filePath)
                .fileSize(file.getSize())
                .uploadedBy(userId)
                .build();

        ClaimDocument savedDocument = claimDocumentRepository.save(document);
        log.info("Document saved with ID: {} for claim ID: {}", savedDocument.getId(), claimId);
        
        return ApiResponse.success("Document uploaded successfully", documentMapper.toResponse(savedDocument));
    }

    @Override
    public ApiResponse<List<DocumentResponse>> getDocumentsByClaim(Long claimId) {
        log.info("Fetching documents for claim ID: {}", claimId);
        
        if (!claimRepository.existsById(claimId)) {
            throw new ResourceNotFoundException("Claim not found with ID: " + claimId);
        }

        List<DocumentResponse> responses = claimDocumentRepository.findByClaimId(claimId).stream()
                .map(documentMapper::toResponse)
                .toList();

        return ApiResponse.success("Documents fetched successfully", responses);
    }
}
