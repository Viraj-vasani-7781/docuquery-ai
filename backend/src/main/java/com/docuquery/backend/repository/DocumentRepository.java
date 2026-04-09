package com.docuquery.backend.repository;

import com.docuquery.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Get all documents of a specific user
    List<Document> findByUserId(Long userId);

    // Get document by id and userId
    // ensures user can only access their own documents
    Optional<Document> findByIdAndUserId(Long id, Long userId);

    // Check if document belongs to user
    boolean existsByIdAndUserId(Long id, Long userId);

    // Get total document count for a user
    long countByUserId(Long userId);

    // Fetch document with extracted text only (avoid loading full entity)
    @Query("SELECT d.extractedText FROM Document d WHERE d.id = :documentId AND d.user.id = :userId")
    Optional<String> findExtractedTextByIdAndUserId(
            @Param("documentId") Long documentId,
            @Param("userId") Long userId
    );
}