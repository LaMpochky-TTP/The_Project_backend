package com.lampochky.dto.response.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InviteAnswerProjectResponseDto extends ResponseDto {
    private Integer projectId;

    public InviteAnswerProjectResponseDto(List<Error> errors, Integer projectId) {
        super(errors);
        this.projectId = projectId;
    }

    public static InviteAnswerProjectResponseDto success(Integer projectId){
        return new InviteAnswerProjectResponseDto(Collections.emptyList(), projectId);
    }

    public static InviteAnswerProjectResponseDto fail(Integer projectId, Error error) {
        return new InviteAnswerProjectResponseDto(Collections.singletonList(error), projectId);
    }

    public static InviteAnswerProjectResponseDto fail(Integer projectId, List<Error> errors){
        return new InviteAnswerProjectResponseDto(errors, projectId);
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
