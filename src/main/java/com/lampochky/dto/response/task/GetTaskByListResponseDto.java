package com.lampochky.dto.response.task;

import com.lampochky.database.entity.TaskList;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetTaskByListResponseDto extends ResponseDto {
    private Integer listId;
    private List<TaskDto> tasks;

    public GetTaskByListResponseDto(List<Error> errors, Integer listId, List<TaskDto> tasks) {
        super(errors);
        this.listId = listId;
        this.tasks = tasks;
    }

    public static GetTaskByListResponseDto success(TaskList taskList){
        return new GetTaskByListResponseDto(Collections.emptyList(), taskList.getId(),
                taskList.getTasks().stream().map(DtoBuilder::buildFullTaskDto).collect(Collectors.toList()));
    }

    public static GetTaskByListResponseDto fail(Integer taskId, Error error){
        return new GetTaskByListResponseDto(Collections.singletonList(error), taskId, Collections.emptyList());
    }

    public static GetTaskByListResponseDto fail(Integer taskId, List<Error> errors){
        return new GetTaskByListResponseDto(errors, taskId, Collections.emptyList());
    }

    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public List<TaskDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDto> tasks) {
        this.tasks = tasks;
    }
}
