package com.docuquery.backend.service.impl;

import com.docuquery.backend.dto.response.DocumentResponse;
import com.docuquery.backend.entity.Document;
import com.docuquery.backend.entity.User;
import com.docuquery.backend.exception.CustomException;
import com.docuquery.backend.repository.ChatHistoryRepository;
import com.docuquery.backend.repository.DocumentRepository;
import com.docuquery.backend.repository.UserRepository;
import com.docuquery.backend.service.DocumentService;
import com.docuquery.backend.service.FileStorageService;
import com.docuquery.backend.service.PdfExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PdfExtractorService pdfExtractorService;
    private final ChatHistoryRepository chatHistoryRepository;

    // ── Upload Document ───────────────────────────────────

    @Override
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, Long userId) {

        // Step 1 — Load user from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        // Step 2 — Extract text from PDF first
        // (validate it's a proper PDF before saving)
        log.info("Extracting text from PDF: {}", file.getOriginalFilename());
        String extractedText = pdfExtractorService.extractText(file);

        // Step 3 — Save file to local storage
        log.info("Saving file to local storage for userId: {}", userId);
        String filePath = fileStorageService.saveFile(file, userId);

        // Step 4 — Save document record to DB
        Document document = Document.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .s3Key(filePath)          // using s3Key column to store local path
                .extractedText(extractedText)
                .fileSize(file.getSize())
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("Document saved with id: {}", savedDocument.getId());

        return mapToResponse(savedDocument);
    }

    // ── Get All Documents ─────────────────────────────────

    @Override
    public List<DocumentResponse> getUserDocuments(Long userId) {

        return documentRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Get Single Document ───────────────────────────────

    @Override
    public DocumentResponse getDocumentById(Long documentId, Long userId) {

        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> CustomException.notFound("Document not found"));

        return mapToResponse(document);
    }

    // ── Delete Document ───────────────────────────────────

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long userId) {

        // Check document exists and belongs to this user
        Document document = documentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> CustomException.notFound("Document not found"));

        // Delete all chat history for this document first
        chatHistoryRepository.deleteByDocumentId(documentId);

        // Delete file from local storage
        fileStorageService.deleteFile(document.getS3Key());

        // Delete document record from DB
        documentRepository.delete(document);

        log.info("Document deleted: {} for userId: {}", documentId, userId);
    }

    // ── Map Entity to Response ────────────────────────────

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .s3Key(document.getS3Key())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}