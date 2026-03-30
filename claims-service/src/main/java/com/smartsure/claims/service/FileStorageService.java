package com.smartsure.claims.service;

import com.smartsure.claims.exception.ClaimsServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = 
            Arrays.asList("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB

    public FileStorageService(
            @Value("${file.upload-dir:uploads/claims}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new ClaimsServiceException(
                "Could not create upload directory.", ex);
        }
    }

    public String storeFile(MultipartFile file, Long claimId) {

        // ✅ Empty check
        if (file.isEmpty()) {
            throw new ClaimsServiceException("Cannot store empty file.");
        }

        // ✅ Size check
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ClaimsServiceException(
                "File size exceeds maximum limit of 10MB.");
        }

        // ✅ Filename check
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new ClaimsServiceException("Invalid file format.");
        }

        // ✅ Extension check
        String extension = originalFilename
                .substring(originalFilename.lastIndexOf(".") + 1)
                .toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ClaimsServiceException(
                "Only PDF, JPG, JPEG, PNG files are allowed.");
        }

        try {
            // ✅ Create claim-specific directory
            Path claimDir = this.fileStorageLocation
                    .resolve(String.valueOf(claimId));
            if (!Files.exists(claimDir)) {
                Files.createDirectories(claimDir);
            }

            // ✅ UUID-based unique filename
            String uniqueFileName = UUID.randomUUID().toString() 
                    + "_" + originalFilename;

            Path targetLocation = claimDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {} for claim ID: {}", 
                    uniqueFileName, claimId);

            return targetLocation.toString();

        } catch (IOException ex) {
            log.error("Failed to store file: {}", ex.getMessage());
            throw new ClaimsServiceException(
                "Could not store file. Please try again!", ex);
        }
    }
}