package com.learn.prompt_chain.model;

public record ResumeMatchRequest(
        String jobDescription,
        String resume
) {}