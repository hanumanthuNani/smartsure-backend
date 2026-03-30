package com.smartsure.claims.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.DocumentResponse;
import com.smartsure.claims.entity.ClaimDocument;
import com.smartsure.claims.exception.ClaimsServiceException;
import com.smartsure.claims.repository.ClaimDocumentRepository;
import com.smartsure.claims.service.ClaimService;
import com.smartsure.claims.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/claims/{claimId}/documents")
@RequiredArgsConstructor
@Tag(name = "Claim Documents", description = "Endpoints for managing evidence and supporting documents for claims")
public class DocumentController {

    private final DocumentService documentService;
    private final ClaimDocumentRepository claimDocumentRepository;
    private final ClaimService claimService;
    private final HttpServletRequest request;

    private String getRoles() { return request.getHeader("X-User-Roles"); }
    private String getEmail() { return request.getHeader("X-User-Email"); }
    private boolean isAdmin() { return getRoles() != null && getRoles().contains("ROLE_ADMIN"); }

    private <T> ResponseEntity<ApiResponse<T>> validateClaimOwnership(Long claimId) {
        if (isAdmin()) return null;

        ApiResponse<com.smartsure.claims.dto.ClaimResponse> claimResponse = claimService.getClaimById(claimId);
        if (claimResponse.getData() == null || !claimResponse.getData().getClaimantEmail().equals(getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: You do not own the parent claim"));
        }
        return null;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "👤 [OWNER] Upload evidence for claim", description = "Uploads a document to support a claim. REJECTS DUPLICATE FILENAMES FOR THE SAME CLAIM.")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file) {
        ResponseEntity<ApiResponse<DocumentResponse>> authResponse = validateClaimOwnership(claimId);
        if (authResponse != null) return authResponse;

        log.info("Uploading document for claim ID: {} by user: {}", claimId, getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(claimId, file, 1L));
    }

    @GetMapping
    @Operation(summary = "👤 [OWNER/ADMIN] List documents", description = "Fetches metadata for all documents uploaded for a specific claim.")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByClaim(
            @PathVariable Long claimId) {
        ResponseEntity<ApiResponse<List<DocumentResponse>>> authResponse = validateClaimOwnership(claimId);
        if (authResponse != null) return authResponse;

        log.info("Fetching documents for claim ID: {}", claimId);
        return ResponseEntity.ok(documentService.getDocumentsByClaim(claimId));
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "👤 [OWNER/ADMIN] Download document", description = "Streams the actual file content for viewing or download.")
    public ResponseEntity<?> downloadDocument(
            @PathVariable Long claimId,
            @PathVariable Long documentId) {

        ResponseEntity<?> authResponse = validateClaimOwnership(claimId);
        if (authResponse != null) return authResponse;

        log.info("Downloading document ID: {} for claim ID: {}", documentId, claimId);

        ClaimDocument document = claimDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ClaimsServiceException("Document not found"));

        if (!document.getClaim().getId().equals(claimId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Document does not belong to the specified claim"));
        }

        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ClaimsServiceException("File not found on server");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Could not download file: {}", e.getMessage());
            throw new ClaimsServiceException("Could not download file: " + e.getMessage());
        }
    }
}