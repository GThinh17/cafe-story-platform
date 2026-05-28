package com.cafestory.dto.responseDTO;

import lombok.Data;

import java.util.List;

@Data
public class UsernameSuggestionResponse {
    private List<String> suggestions;
}
