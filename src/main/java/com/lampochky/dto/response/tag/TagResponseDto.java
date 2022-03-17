package com.lampochky.dto.response.tag;

import com.lampochky.database.entity.Tag;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class TagResponseDto extends ResponseDto {
    private TagDto tag;

    private TagResponseDto(List<Error> errors, TagDto tag) {
        super(errors);
        this.tag = tag;
    }

    public static TagResponseDto success(Tag tag){
        return new TagResponseDto(Collections.emptyList(), DtoBuilder.buildTagDto(tag));
    }

    public static TagResponseDto fail(Error error){
        return new TagResponseDto(Collections.singletonList(error), null);
    }

    public static TagResponseDto fail(List<Error> errors){
        return new TagResponseDto(errors, null);
    }

    public TagDto getTag() {
        return tag;
    }

    public void setTag(TagDto tag) {
        this.tag = tag;
    }
}
