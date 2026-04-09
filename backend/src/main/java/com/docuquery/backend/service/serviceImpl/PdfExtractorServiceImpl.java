package com.docuquery.backend.service.serviceImpl;

import com.docuquery.backend.exception.CustomException;
import com.docuquery.backend.service.PdfExtractorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class PdfExtractorServiceImpl implements PdfExtractorService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    @Override
    public String extractText(MultipartFile file) {

        // Step 1 — Validate file is not empty
        if (file == null || file.isEmpty()) {
            throw CustomException.badRequest("File is empty");
        }

        // Step 2 — Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw CustomException.badRequest("File size exceeds 10MB limit");
        }

        // Step 3 — Validate file is a PDF
        String contentType = file.getContentType();
        if (contentType == null || !PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
            throw CustomException.badRequest("Only PDF files are allowed");
        }

        try {
            // 🔥 FIX: Convert MultipartFile → byte[]
            byte[] pdfBytes = file.getBytes();

            // Load PDF using PDFBox 3.x
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {

                // Step 4 — Check PDF is not empty
                if (document.getNumberOfPages() == 0) {
                    throw CustomException.badRequest("PDF has no pages");
                }

                // Step 5 — Extract text
                PDFTextStripper stripper = new PDFTextStripper();
                String extractedText = stripper.getText(document);

                // Step 6 — Handle scanned PDFs (no selectable text)
                if (extractedText == null || extractedText.trim().isEmpty()) {
                    throw CustomException.badRequest(
                            "No text found in PDF. Scanned image PDFs are not supported yet."
                    );
                }

                log.info("Extracted {} characters from PDF: {}",
                        extractedText.length(),
                        file.getOriginalFilename());

                return extractedText.trim();
            }

        } catch (CustomException e) {
            throw e; // rethrow custom exceptions

        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage(), e);
            throw CustomException.internalError("Failed to read PDF file");
        }
    }

}
