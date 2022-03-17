package com.lampochky.dto.request.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lampochky.database.entity.UserRole;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InviteProjectRequestDto {
    @JsonProperty(required = true)
    private String userIdentifier; // username or email

    @JsonProperty(required = true)
    private UserRole role;

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
