package com.lampochky.dto.response.tag;

import com.lampochky.database.entity.Tag;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class GetTagByIdResponseDto extends ResponseDto {
    private Integer id;
    private TagDto tag;

    private GetTagByIdResponseDto(List<Error> errors, Integer id, TagDto tag) {
        super(errors);
        this.id = id;
        this.tag = tag;
    }

    public static GetTagByIdResponseDto success(Tag tag){
        return new GetTagByIdResponseDto(Collections.emptyList(), tag.getId(), DtoBuilder.buildTagDto(tag));
    }

    public static GetTagByIdResponseDto fail(Integer id, Error error){
        return new GetTagByIdResponseDto(Collections.singletonList(error), id, null);
    }

    public static GetTagByIdResponseDto fail(Integer id, List<Error> errors){
        return new GetTagByIdResponseDto(errors, id, null);
    }

    public TagDto getTag() {
        return tag;
    }

    public void setTag(TagDto tag) {
        this.tag = tag;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

