package com.lampocky.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponseDto {
    private String username;
    private String email;
    private String token;
    private boolean success;
    private String error;

    public LoginResponseDto(String username, String email, String token, boolean success, String error) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.success = success;
        this.error = error;
    }

    public static LoginResponseDto success(String username, String email, String token){
        return new LoginResponseDto(username, email, token, true, null);
    }

    public static LoginResponseDto fail(String email, String error){
        return new LoginResponseDto(null, email, null, false, error);
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
