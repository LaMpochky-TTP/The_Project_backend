package com.lampochky.dto.response.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lampochky.database.entity.Project;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectResponseDto extends ResponseDto {
    private ProjectDto project;

    public ProjectResponseDto(List<Error> errors, ProjectDto project) {
        super(errors);
        this.project = project;
    }

    public static ProjectResponseDto success(Project project) {
        return new ProjectResponseDto(Collections.emptyList(), DtoBuilder.buildShortProjectDto(project));
    }

    public static ProjectResponseDto fail(Error error){
        return new ProjectResponseDto(Collections.singletonList(error), null);
    }

    public static ProjectResponseDto fail(List<Error> errors){
        return new ProjectResponseDto(errors, null);
    }

    public ProjectDto getProject() {
        return project;
    }

    public void setProject(ProjectDto project) {
        this.project = project;
    }
}
