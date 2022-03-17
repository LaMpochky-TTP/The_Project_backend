package com.lampochky.dto.response.aurh;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponseDto extends ResponseDto {
    private String username;
    private String email;
    private String token;

    public LoginResponseDto(List<Error> errors, String username, String email, String token) {
        super(errors);
        this.username = username;
        this.email = email;
        this.token = token;
    }

    public static LoginResponseDto success(String username, String email, String token){
        return new LoginResponseDto(Collections.emptyList(), username, email, token);
    }

    public static LoginResponseDto fail(String email, Error error){
        return new LoginResponseDto(Collections.singletonList(error), null, email, null);
    }

    public static LoginResponseDto fail(String email, List<Error> errors){
        return new LoginResponseDto(errors, null, email, null);
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
