package com.lampochky.dto.response.list;

import com.lampochky.database.entity.TaskList;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class GetListByIdResponseDto extends ResponseDto {
    private Integer id;
    private ListDto list;

    private GetListByIdResponseDto(List<Error> errors, Integer id, ListDto list) {
        super(errors);
        this.id = id;
        this.list = list;
    }

    public static GetListByIdResponseDto success(TaskList taskList){
        return new GetListByIdResponseDto(Collections.emptyList(),
                taskList.getId(), DtoBuilder.buildFullListDto(taskList));
    }

    public static GetListByIdResponseDto fail(Integer id, Error error) {
        return new GetListByIdResponseDto(Collections.singletonList(error), id, null);
    }

    public static GetListByIdResponseDto fail(Integer id, List<Error> errors) {
        return new GetListByIdResponseDto(errors, id, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ListDto getList() {
        return list;
    }

    public void setList(ListDto list) {
        this.list = list;
    }
}
