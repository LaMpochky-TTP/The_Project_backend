package com.lampochky.dto.response.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lampochky.dto.DtoConstants;
import com.lampochky.dto.response.user.UserDto;

import java.time.LocalDateTime;

public class MessageDto {
    private Integer id;
    private String text;
    private UserDto user;
    private Integer taskId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DtoConstants.dateTimeFormat)
    private LocalDateTime dateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
