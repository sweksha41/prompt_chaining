package com.learn.prompt_chain.controller;

import com.learn.prompt_chain.model.ResumeMatchRequest;
import com.learn.prompt_chain.model.ResumeMatchResult;
import com.learn.prompt_chain.service.GroqStreamingService;
import com.learn.prompt_chain.service.PromptChainService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/agent")
public class PromptChainController {

    private final PromptChainService promptChainService;
    private final GroqStreamingService groqStreamingService;


    public PromptChainController(PromptChainService promptChainService,
                                 GroqStreamingService groqStreamingService) {
        this.promptChainService = promptChainService;
        this.groqStreamingService = groqStreamingService;
    }

    @PostMapping("/match")
    public Mono<ResumeMatchResult> matchResume(
            @RequestBody ResumeMatchRequest request) {
        return promptChainService.matchResume(request);
    }


    @GetMapping(
            value = "/ask/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<String>> streamAnswer(
            @RequestParam String prompt
    ) {
        return groqStreamingService.streamAnswer(prompt)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }
}