package com.lampochky.dto.response.project;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.UserRole;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class GetProjectByIdResponseDto extends ResponseDto {
    private Integer id;
    private ProjectDto project;

    private GetProjectByIdResponseDto(List<Error> errors, Integer id, ProjectDto project) {
        super(errors);
        this.id = id;
        this.project = project;
    }

    public static GetProjectByIdResponseDto success(Project project, UserRole role){
        return new GetProjectByIdResponseDto(Collections.emptyList(),
                project.getId(), DtoBuilder.buildFullProjectDto(project, role, true));
    }

    public static GetProjectByIdResponseDto fail(Integer id, Error error){
        return new GetProjectByIdResponseDto(Collections.singletonList(error),
                id, null);
    }

    public static GetProjectByIdResponseDto fail(Integer id, List<Error> errors){
        return new GetProjectByIdResponseDto(errors, id, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProjectDto getProject() {
        return project;
    }

    public void setProject(ProjectDto project) {
        this.project = project;
    }
}
