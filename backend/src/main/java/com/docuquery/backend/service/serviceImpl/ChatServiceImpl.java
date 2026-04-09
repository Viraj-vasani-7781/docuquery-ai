package com.docuquery.backend.service.impl;

import com.docuquery.backend.dto.request.ChatRequest;
import com.docuquery.backend.dto.response.ChatResponse;
import com.docuquery.backend.entity.ChatHistory;
import com.docuquery.backend.entity.Document;
import com.docuquery.backend.entity.User;
import com.docuquery.backend.exception.CustomException;
import com.docuquery.backend.repository.ChatHistoryRepository;
import com.docuquery.backend.repository.DocumentRepository;
import com.docuquery.backend.repository.UserRepository;
import com.docuquery.backend.service.AiService;
import com.docuquery.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    // ── Ask Question ──────────────────────────────────────

    @Override
    @Transactional
    public ChatResponse askQuestion(ChatRequest request, Long userId) {

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        // Load document — ensure it belongs to this user
        Document document = documentRepository
                .findByIdAndUserId(request.getDocumentId(), userId)
                .orElseThrow(() -> CustomException.notFound("Document not found"));

        // Get extracted text from document
        String extractedText = document.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw CustomException.badRequest("Document has no extracted text");
        }

        // Call Gemini AI
        log.info("Asking AI question for documentId: {}", document.getId());
        String answer = aiService.askQuestion(extractedText, request.getQuestion());

        // Save to chat history
        ChatHistory chatHistory = ChatHistory.builder()
                .document(document)
                .user(user)
                .question(request.getQuestion())
                .answer(answer)
                .build();

        ChatHistory saved = chatHistoryRepository.save(chatHistory);

        return mapToResponse(saved);
    }

    // ── Get Chat History ──────────────────────────────────

    @Override
    public List<ChatResponse> getChatHistory(Long documentId, Long userId) {

        // Verify document belongs to user
        if (!documentRepository.existsByIdAndUserId(documentId, userId)) {
            throw CustomException.notFound("Document not found");
        }

        return chatHistoryRepository
                .findByDocumentIdAndUserIdOrderByAskedAtDesc(documentId, userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Delete Chat History ───────────────────────────────

    @Override
    @Transactional
    public void deleteChatHistory(Long documentId, Long userId) {

        if (!documentRepository.existsByIdAndUserId(documentId, userId)) {
            throw CustomException.notFound("Document not found");
        }

        chatHistoryRepository.deleteByDocumentId(documentId);
        log.info("Chat history deleted for documentId: {}", documentId);
    }

    // ── Mapper ────────────────────────────────────────────

    private ChatResponse mapToResponse(ChatHistory chat) {
        return ChatResponse.builder()
                .id(chat.getId())
                .documentId(chat.getDocument().getId())
                .question(chat.getQuestion())
                .answer(chat.getAnswer())
                .askedAt(chat.getAskedAt())
                .build();
    }
}