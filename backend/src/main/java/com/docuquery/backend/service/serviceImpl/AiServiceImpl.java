package com.docuquery.backend.service.impl;

import com.docuquery.backend.exception.CustomException;
import com.docuquery.backend.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final WebClient geminiWebClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    // Max characters of document text to send to AI
    // Gemini free tier has token limits
    private static final int MAX_CONTEXT_LENGTH = 10000;

    @Override
    public String askQuestion(String documentText, String question) {

        // Step 1 — Trim document text if too long
        String context = documentText.length() > MAX_CONTEXT_LENGTH
                ? documentText.substring(0, MAX_CONTEXT_LENGTH) + "..."
                : documentText;

        // Step 2 — Build the prompt
        String prompt = buildPrompt(context, question);

        // Step 3 — Build Gemini request body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,      // lower = more factual answers
                        "maxOutputTokens", 1024  // limit response length
                )
        );

        // Step 4 — Call Gemini API
        try {
            Map response = geminiWebClient
                    .post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Step 5 — Extract answer from response
            return extractAnswerFromResponse(response);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw CustomException.internalError("AI service is unavailable. Please try again.");
        }
    }

    // ── Build Prompt ─────────────────────────────────────

    private String buildPrompt(String documentText, String question) {
        return """
                You are a helpful assistant that answers questions based ONLY on the provided document content.
                
                RULES:
                - Answer ONLY from the document content below
                - If the answer is not in the document, say "I could not find this information in the document"
                - Be concise and clear
                - Do not make up information
                
                DOCUMENT CONTENT:
                %s
                
                USER QUESTION:
                %s
                
                ANSWER:
                """.formatted(documentText, question);
    }

    // ── Parse Gemini Response ─────────────────────────────

    @SuppressWarnings("unchecked")
    private String extractAnswerFromResponse(Map response) {
        try {
            // Gemini response structure:
            // { candidates: [ { content: { parts: [ { text: "answer" } ] } } ] }
            List<Map> candidates = (List<Map>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw CustomException.internalError("No response from AI");
            }

            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            String answer = (String) parts.get(0).get("text");

            if (answer == null || answer.trim().isEmpty()) {
                throw CustomException.internalError("Empty response from AI");
            }

            return answer.trim();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw CustomException.internalError("Failed to read AI response");
        }
    }
}