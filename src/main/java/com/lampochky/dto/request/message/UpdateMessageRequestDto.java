package com.lampochky.dto.request.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateMessageRequestDto {
    @JsonProperty(required = true)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
