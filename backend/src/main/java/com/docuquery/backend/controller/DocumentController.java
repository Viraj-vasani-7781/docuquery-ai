package com.docuquery.backend.controller;

import com.docuquery.backend.dto.response.DocumentResponse;
import com.docuquery.backend.entity.User;
import com.docuquery.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // ── Upload PDF ────────────────────────────────────────

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                documentService.uploadDocument(file, currentUser.getId())
        );
    }

    // ── Get All My Documents ──────────────────────────────

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                documentService.getUserDocuments(currentUser.getId())
        );
    }

    // ── Get Single Document ───────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                documentService.getDocumentById(id, currentUser.getId())
        );
    }

    // ── Delete Document ───────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        documentService.deleteDocument(id, currentUser.getId());
        return ResponseEntity.ok("Document deleted successfully");
    }
}