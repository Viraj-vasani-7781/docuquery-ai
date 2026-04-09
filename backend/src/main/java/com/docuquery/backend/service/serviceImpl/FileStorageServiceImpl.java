package com.docuquery.backend.service.impl;

import com.docuquery.backend.exception.CustomException;
import com.docuquery.backend.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public String saveFile(MultipartFile file, Long userId) {

        try {
            // Create user-specific folder: uploads/documents/userId/
            Path userFolder = Paths.get(uploadDir, String.valueOf(userId));
            Files.createDirectories(userFolder);

            // Generate unique filename to avoid conflicts
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = userFolder.resolve(uniqueFileName);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath);

            log.info("File saved at: {}", filePath);

            // Return relative path to store in DB
            return userId + "/" + uniqueFileName;

        } catch (IOException e) {
            throw CustomException.internalError("Failed to save file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDir, filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", path);
        } catch (IOException e) {
            throw CustomException.internalError("Failed to delete file: " + e.getMessage());
        }
    }
}