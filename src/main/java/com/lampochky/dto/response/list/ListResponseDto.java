package com.lampochky.dto.response.list;

import com.lampochky.database.entity.TaskList;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class ListResponseDto extends ResponseDto {
    private ListDto list;

    private ListResponseDto(List<Error> errors, ListDto list) {
        super(errors);
        this.list = list;
    }

    public static ListResponseDto success(TaskList taskList) {
        return new ListResponseDto(Collections.emptyList(), DtoBuilder.buildShortListDto(taskList));
    }

    public static ListResponseDto fail(Error error) {
        return new ListResponseDto(Collections.singletonList(error), null);
    }

    public static ListResponseDto fail(List<Error> errors) {
        return new ListResponseDto(errors, null);
    }

    public ListDto getList() {
        return list;
    }

    public void setList(ListDto list) {
        this.list = list;
    }
}
