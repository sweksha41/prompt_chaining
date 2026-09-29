package com.learn.prompt_chain.model;

public record Message(
        String role,
        String content
) {}
