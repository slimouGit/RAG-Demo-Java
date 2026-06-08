package org.example.ragdemojava;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Service
public class OllamaService {

    private final RestClient restClient;

    public OllamaService(@Value("${ollama.base-url}") String ollamaBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
    }

    public List<OllamaDtos.ModelInfo> getAvailableModels() {
        OllamaDtos.TagsResponse response = restClient.get()
                .uri("/api/tags")
                .retrieve()
                .body(OllamaDtos.TagsResponse.class);

        if (response == null || response.models() == null) {
            return List.of();
        }

        return response.models();
    }

    public String detectModelName() {
        List<OllamaDtos.ModelInfo> models = getAvailableModels();

        if (models.isEmpty()) {
            throw new IllegalStateException("Kein lokales Ollama-Modell gefunden. Bitte zuerst z. B. `ollama pull llama3.2` ausführen.");
        }

        return models.stream()
                .sorted(Comparator.comparing(OllamaDtos.ModelInfo::modified_at, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(model -> model.name() != null ? model.name() : model.model())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Kein gültiger Modellname gefunden."));
    }

    public String ask(String documentText, String question) {
        String model = detectModelName();

        String prompt = """
                Du bist ein RAG-Assistent.
                Beantworte die Frage ausschließlich auf Basis des bereitgestellten Dokuments.
                Wenn die Antwort nicht im Dokument steht, sage: "Diese Information steht nicht im Dokument."

                DOKUMENT:
                %s

                FRAGE:
                %s

                ANTWORT:
                """.formatted(limitText(documentText, 12000), question);

        OllamaDtos.GenerateRequest request = new OllamaDtos.GenerateRequest(model, prompt, false);

        OllamaDtos.GenerateResponse response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(OllamaDtos.GenerateResponse.class);

        if (response == null || response.response() == null) {
            return "Keine Antwort von Ollama erhalten.";
        }

        return response.response();
    }

    private String limitText(String text, int maxChars) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxChars) {
            return text;
        }

        return text.substring(0, maxChars) + "\n\n[Dokument wurde gekürzt.]";
    }
}