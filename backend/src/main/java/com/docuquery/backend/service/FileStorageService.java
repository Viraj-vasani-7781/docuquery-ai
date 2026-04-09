package com.docuquery.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // Save file to local folder, return file path
    String saveFile(MultipartFile file, Long userId);

    // Delete file from local folder
    void deleteFile(String filePath);
}