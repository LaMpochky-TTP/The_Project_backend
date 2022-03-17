package com.lampochky.dto.response.task;

import com.lampochky.database.entity.Task;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class TaskResponseDto extends ResponseDto {
    private TaskDto task;

    public TaskResponseDto(List<Error> errors, TaskDto task) {
        super(errors);
        this.task = task;
    }

    public static TaskResponseDto success(Task task){
        return new TaskResponseDto(Collections.emptyList(), DtoBuilder.buildShortTaskDto(task));
    }

    public static TaskResponseDto fail(Error error) {
        return new TaskResponseDto(Collections.singletonList(error), null);
    }

    public static TaskResponseDto fail(List<Error> errors) {
        return new TaskResponseDto(errors, null);
    }

    public TaskDto getTask() {
        return task;
    }

    public void setTask(TaskDto task) {
        this.task = task;
    }
}
