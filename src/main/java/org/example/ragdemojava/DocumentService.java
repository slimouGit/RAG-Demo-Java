package org.example.ragdemojava;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
public class DocumentService {

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Bitte eine Datei hochladen.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException("Dateiname fehlt.");
        }

        try {
            if (filename.toLowerCase().endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            if (filename.toLowerCase().endsWith(".pdf")) {
                try (var document = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(document);
                }
            }

            throw new IllegalArgumentException("Nur .txt und .pdf werden unterstützt.");

        } catch (Exception e) {
            throw new RuntimeException("Dokument konnte nicht gelesen werden: " + e.getMessage(), e);
        }
    }
}