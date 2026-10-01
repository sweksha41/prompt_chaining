package com.learn.prompt_chain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GroqStreamingService {

    private static final String MODEL = "openai/gpt-oss-120b";

    private final WebClient webClient;
    private final String apiKey;

    public GroqStreamingService(
            WebClient groqWebClient,
            @Value("${groq.api.key}") String apiKey
    ) {
        this.webClient = groqWebClient;
        this.apiKey = apiKey;
    }

    public Flux<String> streamAnswer(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "stream", true
        );

        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(
                        new ParameterizedTypeReference<ServerSentEvent<String>>() {}
                )
                .map(ServerSentEvent::data)
                .filter(data -> !"[DONE]".equals(data))
                .map(this::extractContent)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(content -> !content.isBlank());
    }


    private Optional<String> extractContent(String eventData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode content = objectMapper.readTree(eventData)
                    .at("/choices/0/delta/content");

            return content.isTextual()
                    ? Optional.of(content.asText())
                    : Optional.empty();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Could not parse a Groq streaming response event",
                    exception
            );
        }
    }
}