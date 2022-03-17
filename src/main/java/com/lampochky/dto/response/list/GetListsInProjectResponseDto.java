package com.lampochky.dto.response.list;


import com.lampochky.database.entity.TaskList;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetListsInProjectResponseDto extends ResponseDto {
    private Integer projectId;
    private List<ListDto> lists;

    public GetListsInProjectResponseDto(List<Error> errors, Integer projectId, List<ListDto> lists) {
        super(errors);
        this.projectId = projectId;
        this.lists = lists;
    }

    public static GetListsInProjectResponseDto success(Integer projectId, List<TaskList> taskLists){
        return new GetListsInProjectResponseDto(Collections.emptyList(),
                projectId, taskLists.stream().map(DtoBuilder::buildFullListDto).collect(Collectors.toList()));
    }

    public static GetListsInProjectResponseDto fail(Integer projectId, Error error){
        return new GetListsInProjectResponseDto(Collections.singletonList(error),
                projectId, Collections.emptyList());
    }

    public static GetListsInProjectResponseDto fail(Integer projectId, List<Error> errors){
        return new GetListsInProjectResponseDto(errors, projectId, Collections.emptyList());
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<ListDto> getLists() {
        return lists;
    }

    public void setLists(List<ListDto> lists) {
        this.lists = lists;
    }
}
