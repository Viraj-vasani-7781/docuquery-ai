package com.docuquery.backend.repository;

import com.docuquery.backend.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    // Get all chats for a specific document (ordered latest first)
    List<ChatHistory> findByDocumentIdOrderByAskedAtDesc(Long documentId);

    // Get all chats by a user across all documents
    List<ChatHistory> findByUserIdOrderByAskedAtDesc(Long userId);

    // Get chats for a specific document by a specific user
    List<ChatHistory> findByDocumentIdAndUserIdOrderByAskedAtDesc(
            Long documentId,
            Long userId
    );

    // Count how many questions asked on a document
    long countByDocumentId(Long documentId);

    // Delete all chat history for a document
    void deleteByDocumentId(Long documentId);
}