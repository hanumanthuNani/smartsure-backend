package com.smartsure.claims.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @Test
    void storeFile_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "content".getBytes());
        
        String path = fileStorageService.storeFile(file, 1L);
        assertNotNull(path);
        assertTrue(path.contains("test.png"));
    }

    @Test
    void storeFile_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "", null, new byte[0]);
        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file, 1L));
    }

    @Test
    void storeFile_LargeFile_ThrowsException() {
        byte[] largeContent = new byte[(10 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile("file", "large.png", "image/png", largeContent);
        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file, 1L));
    }

    @Test
    void storeFile_InvalidExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/x-msdownload", "content".getBytes());
        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file, 1L));
    }

    @Test
    void storeFile_NoExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test", null, "content".getBytes());
        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file, 1L));
    }

    @Test
    void storeFile_IOException_ThrowsException() throws java.io.IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "content".getBytes());
        
        // Use an invalid claim ID or path that causes IOException
        // On Windows, a filename with invalid characters or trying to overwrite a directory might work.
        // But the easiest is to make the claim directory a file instead.
        java.nio.file.Path claimDir = tempDir.resolve("2");
        java.nio.file.Files.createFile(claimDir); 
        
        // Now FileStorageService will try to create directory '2' but it's already a file!
        assertThrows(RuntimeException.class, () -> fileStorageService.storeFile(file, 2L));
    }
}
