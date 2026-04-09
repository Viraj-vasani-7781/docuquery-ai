package com.docuquery.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface PdfExtractorService {

    // Extract all text from uploaded PDF file
    String extractText(MultipartFile file);
}