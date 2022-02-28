package com.lampocky.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterResponseDto {
    private String username;
    private String email;
    private String token;
    private boolean success;
    private List<String> errors;

    private RegisterResponseDto(String username, String email, String token, boolean success, List<String> errors) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.success = success;
        this.errors = errors;
    }

    public static RegisterResponseDto success(String username, String email, String token){
        return new RegisterResponseDto(username, email, token, true, null);
    }

    public static RegisterResponseDto fail(String username, String email, List<String> errors){
        return new RegisterResponseDto(username, email, null, false, new ArrayList<>(errors));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
