package org.example.ragdemojava;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class RagController {

    private final DocumentService documentService;
    private final OllamaService ollamaService;

    public RagController(DocumentService documentService, OllamaService ollamaService) {
        this.documentService = documentService;
        this.ollamaService = ollamaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        addModelInfo(model);
        return "index";
    }

    @PostMapping("/ask")
    public String ask(
            @RequestParam("file") MultipartFile file,
            @RequestParam("question") String question,
            Model model
    ) {
        try {
            String documentText = documentService.extractText(file);
            String answer = ollamaService.ask(documentText, question);

            model.addAttribute("question", question);
            model.addAttribute("answer", answer);
            model.addAttribute("documentPreview", preview(documentText));

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        addModelInfo(model);
        return "index";
    }

    private void addModelInfo(Model model) {
        try {
            List<OllamaDtos.ModelInfo> models = ollamaService.getAvailableModels();
            model.addAttribute("models", models);
            model.addAttribute("activeModel", models.isEmpty() ? null : ollamaService.detectModelName());
        } catch (Exception e) {
            model.addAttribute("models", List.of());
            model.addAttribute("modelError", "Ollama nicht erreichbar oder kein Modell installiert.");
        }
    }

    private String preview(String text) {
        if (text == null) {
            return "";
        }

        return text.length() <= 1000 ? text : text.substring(0, 1000) + "...";
    }
}