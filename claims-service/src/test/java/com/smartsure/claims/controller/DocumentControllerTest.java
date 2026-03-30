package com.smartsure.claims.controller;

import com.smartsure.claims.dto.ApiResponse;
import com.smartsure.claims.dto.DocumentResponse;
import com.smartsure.claims.entity.ClaimDocument;
import com.smartsure.claims.repository.ClaimDocumentRepository;
import com.smartsure.claims.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private ClaimDocumentRepository claimDocumentRepository;

    @Test
    void uploadDocument_Returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "data".getBytes());
        when(documentService.uploadDocument(anyLong(), any(), anyLong()))
                .thenReturn(ApiResponse.success("Uploaded", new DocumentResponse()));

        mockMvc.perform(multipart("/api/claims/1/documents")
                .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getDocumentsByClaim_Returns200() throws Exception {
        when(documentService.getDocumentsByClaim(1L)).thenReturn(ApiResponse.success("Found", Collections.emptyList()));

        mockMvc.perform(get("/api/claims/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void downloadDocument_ReturnsFile() throws Exception {
        // Create a real temporary file for the test
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-doc", ".txt");
        java.nio.file.Files.write(tempFile, "hello world".getBytes());

        ClaimDocument doc = ClaimDocument.builder()
                .id(1L)
                .fileName("test.txt")
                .fileType("text/plain")
                .filePath(tempFile.toAbsolutePath().toString())
                .build();

        when(claimDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));

        mockMvc.perform(get("/api/claims/1/documents/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello world"));

        java.nio.file.Files.deleteIfExists(tempFile);
    }

    @Test
    void downloadDocument_NotFound_Returns400() throws Exception {
        when(claimDocumentRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/claims/1/documents/1/download"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downloadDocument_FileNotFoundOnServer_Returns400() throws Exception {
        ClaimDocument doc = ClaimDocument.builder()
                .id(1L)
                .filePath("non-existent-file.txt")
                .build();
        when(claimDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));

        mockMvc.perform(get("/api/claims/1/documents/1/download"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File not found on server"));
    }
}
