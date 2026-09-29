package com.learn.prompt_chain.service;

import com.learn.prompt_chain.model.GroqResponse;
import com.learn.prompt_chain.model.ResumeMatchRequest;
import com.learn.prompt_chain.model.ResumeMatchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class PromptChainService {

    private static final String MODEL = "openai/gpt-oss-120b";

    private final WebClient webClient;
    private final String apiKey;

    public PromptChainService(WebClient groqWebClient,
                              @Value("${groq.api.key}") String apiKey) {
        this.webClient = groqWebClient;
        this.apiKey = apiKey;
    }

    public Mono<ResumeMatchResult> matchResume(ResumeMatchRequest request) {
        String extractionSystemPrompt = """
                You are a professional HR assistant. Extract the skills from the provided text.
                Only return skills explicitly stated in the text. Do not invent any skills.
                Return only comma-separated skills with no other information.
                """;

        String resumePrompt = "Extract the skills from this resume:\n" + request.resume();
        String jobDescriptionPrompt =
                "Extract the skills from this job description:\n" + request.jobDescription();

        return askForText(extractionSystemPrompt, resumePrompt)
                .flatMap(candidateSkills ->
                        askForText(extractionSystemPrompt, jobDescriptionPrompt)
                                .flatMap(jobDescriptionSkills -> {
                                    String comparisonSystemPrompt = """
                                            You are a professional HR assistant. Compare the candidate's skills
                                            with the skills required by the job description. Give a score from
                                            1 to 100 and a short verdict on whether the candidate is a good fit.
                                            """;

                                    String comparisonPrompt = """
                                            Compare these skills.

                                            Job description skills:
                                            %s

                                            Candidate skills:
                                            %s
                                            """.formatted(jobDescriptionSkills, candidateSkills);

                                    return askForText(
                                            comparisonSystemPrompt,
                                            comparisonPrompt
                                    ).map(evaluation -> new ResumeMatchResult(
                                            candidateSkills,
                                            jobDescriptionSkills,
                                            evaluation
                                    ));
                                })
                );
    }

    private Mono<String> askForText(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt)
                .map(response -> response.choices().getFirst().message().content());
    }

    private Mono<GroqResponse> chat(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2,
                "max_tokens", 1000
        );

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GroqResponse.class);
    }
}