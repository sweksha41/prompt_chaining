
package com.learn.prompt_chain.model;

import java.util.List;

public record GroqResponse(
        List<Choice> choices,
        Usage usage
) {}
