package com.lampochky.dto.response.tag;

import com.lampochky.database.entity.Tag;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetTagsInProjectResponseDto extends ResponseDto {
    private Integer projectId;
    private List<TagDto> tags;

    private GetTagsInProjectResponseDto(List<Error> errors, Integer projectId, List<TagDto> tags) {
        super(errors);
        this.projectId = projectId;
        this.tags = tags;
    }

    public static GetTagsInProjectResponseDto success(Integer projectId, List<Tag> tags) {
        return new GetTagsInProjectResponseDto(Collections.emptyList(), projectId,
                tags.stream().map(DtoBuilder::buildTagDto).collect(Collectors.toList()));
    }

    public static GetTagsInProjectResponseDto fail(Integer projectId, Error error) {
        return new GetTagsInProjectResponseDto(Collections.singletonList(error), projectId, Collections.emptyList());
    }

    public static GetTagsInProjectResponseDto fail(Integer projectId, List<Error> errors) {
        return new GetTagsInProjectResponseDto(errors, projectId, Collections.emptyList());
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }
}
