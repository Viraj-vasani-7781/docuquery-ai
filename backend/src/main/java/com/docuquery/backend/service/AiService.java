package com.docuquery.backend.service;

public interface AiService {

    // Send document text + user question → get AI answer
    String askQuestion(String documentText, String question);
}