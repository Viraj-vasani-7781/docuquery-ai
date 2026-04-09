package com.docuquery.backend.controller;

import com.docuquery.backend.dto.request.ChatRequest;
import com.docuquery.backend.dto.response.ChatResponse;
import com.docuquery.backend.entity.User;
import com.docuquery.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ── Ask Question ──────────────────────────────────────

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> ask(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                chatService.askQuestion(request, currentUser.getId())
        );
    }

    // ── Get Chat History ──────────────────────────────────

    @GetMapping("/history/{documentId}")
    public ResponseEntity<List<ChatResponse>> getHistory(
            @PathVariable Long documentId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                chatService.getChatHistory(documentId, currentUser.getId())
        );
    }

    // ── Delete Chat History ───────────────────────────────

    @DeleteMapping("/history/{documentId}")
    public ResponseEntity<String> deleteHistory(
            @PathVariable Long documentId,
            @AuthenticationPrincipal User currentUser) {

        chatService.deleteChatHistory(documentId, currentUser.getId());
        return ResponseEntity.ok("Chat history cleared");
    }
}