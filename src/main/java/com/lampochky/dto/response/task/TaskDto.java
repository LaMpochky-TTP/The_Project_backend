package com.lampochky.dto.response.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lampochky.dto.DtoConstants;
import com.lampochky.dto.response.message.MessageDto;
import com.lampochky.dto.response.tag.TagDto;
import com.lampochky.dto.response.user.UserDto;

import java.time.LocalDate;
import java.util.List;

public class TaskDto {
    @JsonProperty(required = true)
    protected Integer id;

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
    protected UserDto assignedUser;

    @JsonProperty(required = true)
    protected UserDto creator;

    @JsonProperty(required = true)
    protected List<TagDto> tags;

    @JsonProperty(required = true)
    protected List<MessageDto> messages;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public UserDto getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(UserDto assignedUser) {
        this.assignedUser = assignedUser;
    }

    public UserDto getCreator() {
        return creator;
    }

    public void setCreator(UserDto creator) {
        this.creator = creator;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}
