package com.learn.prompt_chain.model;

public record ResumeMatchResult(
        String candidateSkills,
        String jobDescriptionSkills,
        String evaluation
) {}