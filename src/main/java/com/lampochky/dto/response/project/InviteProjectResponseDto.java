package com.lampochky.dto.response.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InviteProjectResponseDto extends ResponseDto {
    private String userIdentifier;
    private Integer projectId;

    public InviteProjectResponseDto(List<Error> errors, String userIdentifier, Integer projectId) {
        super(errors);
        this.userIdentifier = userIdentifier;
        this.projectId = projectId;
    }

    public static InviteProjectResponseDto success(String userIdentifier, Integer projectId) {
        return new InviteProjectResponseDto(Collections.emptyList(), userIdentifier, projectId);
    }

    public static InviteProjectResponseDto fail(String userIdentifier, Integer projectId, List<Error> errors) {
        return new InviteProjectResponseDto(errors, userIdentifier, projectId);
    }

    public static InviteProjectResponseDto fail(String userIdentifier, Integer projectId, Error error) {
        return new InviteProjectResponseDto(Collections.singletonList(error), userIdentifier, projectId);
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
