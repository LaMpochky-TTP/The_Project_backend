package com.lampochky.dto.request.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lampochky.dto.DtoConstants;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskRequestDto {
    @JsonProperty(required = true)
    protected String name;

    @JsonProperty(required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DtoConstants.dateFormat)
    protected LocalDate dateToStart;

    @JsonProperty(required = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DtoConstants.dateFormat)
    protected LocalDate dateToFinish;

    @JsonProperty(required = true)
    protected Integer priority;

    @JsonProperty(required = true)
    protected String description;

    @JsonProperty(required = true)
    protected Integer listId;

    @JsonProperty(required = true)
    protected Integer assignedUserId;

    @JsonProperty(required = true)
    protected List<Integer> tagIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateToStart() {
        return dateToStart;
    }

    public void setDateToStart(LocalDate dateToStart) {
        this.dateToStart = dateToStart;
    }

    public LocalDate getDateToFinish() {
        return dateToFinish;
    }

    public void setDateToFinish(LocalDate dateToFinish) {
        this.dateToFinish = dateToFinish;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getListId() {
        return listId;
    }

    public void setListId(Integer listId) {
        this.listId = listId;
    }

    public Integer getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Integer assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public List<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Integer> tagIds) {
        this.tagIds = tagIds;
    }
}
