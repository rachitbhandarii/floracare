package com.floracare.mvp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String processContent(AiRequest request) {
        // Build text part of prompt
        String prompt = buildPrompt(request);

        // Handle base64 image if present
        Map<String, Object> imagePart = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imagePart = Map.of(
                    "inline_data", Map.of(
                            "mime_type", "image/jpeg", // or "image/png"
                            "data", request.getImage()
                    )
            );
        }

        // Build "parts" list with optional image + text
        List<Object> parts = new java.util.ArrayList<>();
        if (imagePart != null) {
            parts.add(imagePart);
        }
        parts.add(Map.of("text", prompt));

        // Build full request body
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", parts)
                )
        );

        // Send POST request
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null
                        && !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
        } catch (Exception e) {
            return "Error Parsing: " + e.getMessage();
        }
        return "No response content.";
    }

    private String buildPrompt(AiRequest request) {
        StringBuilder prompt = new StringBuilder();

        switch (request.getOperation()) {
            case "image diagnosis":
                prompt.append("Imagine you are a specialized and experienced agriculturist. Help me in providing diagnosis of the image of the plant provided. What is the plant's name, what is its species,and what is the correct procedure to take care of the plant. Tell if the plant's health looks good, if not what could be done to improve it. Give the answer in this format only. \n\n");
                break;
            case "image detection":
                prompt.append("Imagine you are a specialized and experienced agriculturist. Image of the plant is provided. What is the plant's name, what is its species. Give only these answers only. \n\n");
                break;
            case "image custom":
                prompt.append("Imagine you are a specialized and experienced agriculturist. Image of the plant is provided. Answer the following question briefly: \n\n");
                break;
            case "text custom":
                prompt.append("Imagine you are a specialized and experienced agriculturist. Answer the following question briefly: \n\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + request.getOperation());
        }

        prompt.append(request.getContent());
        return prompt.toString();
    }
}
