package com.docuquery.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotBlank(message = "Question is required")
    private String question;
}