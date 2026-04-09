package com.docuquery.backend.service;

import com.docuquery.backend.dto.request.ChatRequest;
import com.docuquery.backend.dto.response.ChatResponse;

import java.util.List;

public interface ChatService {

    ChatResponse askQuestion(ChatRequest request, Long userId);

    List<ChatResponse> getChatHistory(Long documentId, Long userId);

    void deleteChatHistory(Long documentId, Long userId);
}