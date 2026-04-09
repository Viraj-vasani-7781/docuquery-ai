package com.docuquery.backend.service;

import com.docuquery.backend.dto.response.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    // Upload PDF and save to local storage
    DocumentResponse uploadDocument(MultipartFile file, Long userId);

    // Get all documents of logged in user
    List<DocumentResponse> getUserDocuments(Long userId);

    // Get single document by id
    DocumentResponse getDocumentById(Long documentId, Long userId);

    // Delete document by id
    void deleteDocument(Long documentId, Long userId);
}