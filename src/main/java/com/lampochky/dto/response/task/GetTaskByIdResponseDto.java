package com.lampochky.dto.response.task;

import com.lampochky.database.entity.Task;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class GetTaskByIdResponseDto extends ResponseDto {
    private Integer id;
    private TaskDto task;

    private GetTaskByIdResponseDto(List<Error> errors, Integer id, TaskDto task) {
        super(errors);
        this.id = id;
        this.task = task;
    }

    public static GetTaskByIdResponseDto success(Task task) {
        return new GetTaskByIdResponseDto(Collections.emptyList(),
                task.getId(), DtoBuilder.buildFullTaskDto(task));
    }

    public static GetTaskByIdResponseDto fail(Integer id, Error error) {
        return new GetTaskByIdResponseDto(Collections.singletonList(error), id, null);
    }

    public static GetTaskByIdResponseDto fail(Integer id, List<Error> errors){
        return new GetTaskByIdResponseDto(errors, id, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TaskDto getTask() {
        return task;
    }

    public void setTask(TaskDto task) {
        this.task = task;
    }
}
