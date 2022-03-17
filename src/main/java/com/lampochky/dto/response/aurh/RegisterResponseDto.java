package com.lampochky.dto.response.aurh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterResponseDto extends ResponseDto {
    private String username;
    private String email;
    private String token;

    public RegisterResponseDto(List<Error> errors, String username, String email, String token) {
        super(errors);
        this.username = username;
        this.email = email;
        this.token = token;
    }

    public static RegisterResponseDto success(String username, String email, String token){
        return new RegisterResponseDto(Collections.emptyList(), username, email, token);
    }

    public static RegisterResponseDto fail(String username, String email, Error error){
        return new RegisterResponseDto(Collections.singletonList(error), username, email, null);
    }

    public static RegisterResponseDto fail(String username, String email, List<Error> errors){
        return new RegisterResponseDto(errors, username, email, null);
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
}
