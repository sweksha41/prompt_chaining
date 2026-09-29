package com.learn.prompt_chain.controller;

import com.learn.prompt_chain.model.ResumeMatchRequest;
import com.learn.prompt_chain.model.ResumeMatchResult;
import com.learn.prompt_chain.service.PromptChainService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/agent")
public class PromptChainController {

    private final PromptChainService promptChainService;


    public PromptChainController(PromptChainService promptChainService) {
        this.promptChainService = promptChainService;
    }

    @PostMapping("/match")
    public Mono<ResumeMatchResult> matchResume(
            @RequestBody ResumeMatchRequest request) {
        return promptChainService.matchResume(request);
    }
}