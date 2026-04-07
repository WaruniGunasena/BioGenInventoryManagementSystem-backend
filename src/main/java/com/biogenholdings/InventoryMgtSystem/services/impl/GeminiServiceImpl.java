package com.biogenholdings.InventoryMgtSystem.services.impl;

import com.biogenholdings.InventoryMgtSystem.services.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {
//    private final WebClient.Builder webClientBuilder;
//    private final ObjectMapper objectMapper; // Spring Boot provides this automatically
//
//    @Value("${gemini.api.key}")
//    private String apiKey;
//
//    public String askGemini(String prompt) {
//        Map<String, Object> body = Map.of(
//                "contents", List.of(
//                        Map.of("parts", List.of(
//                                Map.of("text", prompt)
//                        ))
//                )
//        );
//
//        String modelId = "gemini-2.5-flash";
//        String url = "https://generativelanguage.googleapis.com/v1/models/" + modelId + ":generateContent?key=" + apiKey;
//
//        String rawResponse = webClientBuilder.build()
//                .post()
//                .uri(url)
//                .bodyValue(body)
//                .header("Content-Type", "application/json")
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        // 🔥 EXTRACTION LOGIC: Get only the 'text' field
//        try {
//            JsonNode root = objectMapper.readTree(rawResponse);
//            return root.path("candidates")
//                    .get(0)
//                    .path("content")
//                    .path("parts")
//                    .get(0)
//                    .path("text")
//                    .asText();
//        } catch (Exception e) {
//            System.err.println("Error parsing Gemini response: " + e.getMessage());
//            return "ERROR_PARSING_RESPONSE";
//        }
//    }
}