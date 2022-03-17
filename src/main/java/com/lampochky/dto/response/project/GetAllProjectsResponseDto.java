package com.lampochky.dto.response.project;

import com.lampochky.database.entity.Project;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetAllProjectsResponseDto extends ResponseDto {
    private List<ProjectDto> projects;

    private GetAllProjectsResponseDto(List<Error> errors, List<ProjectDto> projects) {
        super(errors);
        this.projects = projects;
    }

    public static GetAllProjectsResponseDto success(List<Project> projects){
        return new GetAllProjectsResponseDto(Collections.emptyList(),
                projects.stream().map(DtoBuilder::buildShortProjectDto).collect(Collectors.toList()));
    }

    public static GetAllProjectsResponseDto fail(Error error){
        return new GetAllProjectsResponseDto(Collections.singletonList(error), Collections.emptyList());
    }

    public static GetAllProjectsResponseDto fail(List<Error> errors) {
        return new GetAllProjectsResponseDto(errors, Collections.emptyList());
    }

    public List<ProjectDto> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDto> projects) {
        this.projects = projects;
    }
}
