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

    private static final String SKILL_EXTRACTION_PROMPT = """
            You are a professional HR assistant. Extract skills explicitly stated
            in the provided text. Do not invent any skills. Return only
            comma-separated skills, with no other information.
            """;

    private static final String SKILL_MATCH_PROMPT = """
            You are a professional HR assistant. Compare the candidate's skills
            with the skills required in the job description. Give a score between
            1 and 100 and a short verdict on whether the candidate is a good fit.
            """;

    public Mono<ResumeMatchResult> matchResume(
            ResumeMatchRequest resumeMatchRequest
    ) {
        return step1ExtractResumeSkills(resumeMatchRequest.resume())
                .flatMap(candidateSkills ->
                        step2ExtractJobDescriptionSkills(resumeMatchRequest.jobDescription())
                                .map(jobSkills ->
                                        new ExtractedSkills(candidateSkills, jobSkills)
                                )
                )
                .flatMap(skills ->
                        step3MatchSkills(
                                skills.candidateSkills(),
                                skills.jobSkills()
                        ).map(evaluation ->
                                new ResumeMatchResult(
                                        skills.candidateSkills(),
                                        skills.jobSkills(),
                                        evaluation
                                )
                        )
                );
    }

    private Mono<String> step1ExtractResumeSkills(String resume) {
        String userPrompt = """
                Extract the skills from this resume:
                %s
                """.formatted(resume);

        return askLlm(SKILL_EXTRACTION_PROMPT, userPrompt);
    }

    private Mono<String> step2ExtractJobDescriptionSkills(String jobDescription) {
        String userPrompt = """
                Extract the skills from this job description:
                %s
                """.formatted(jobDescription);

        return askLlm(SKILL_EXTRACTION_PROMPT, userPrompt);
    }

    private Mono<String> step3MatchSkills(
            String candidateSkills,
            String jobSkills
    ) {
        String userPrompt = """
                Compare these skills.

                Job description skills:
                %s

                Candidate skills:
                %s
                """.formatted(jobSkills, candidateSkills);

        return askLlm(SKILL_MATCH_PROMPT, userPrompt);
    }

    private Mono<String> askLlm(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt)
                .map(response -> response.choices().getFirst().message().content());
    }

    private Mono<GroqResponse> chat(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
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
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GroqResponse.class);
    }
    private record ExtractedSkills(
            String candidateSkills,
            String jobSkills
    ) {}

}