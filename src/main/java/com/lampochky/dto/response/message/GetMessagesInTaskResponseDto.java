package com.lampochky.dto.response.message;

import com.lampochky.database.entity.Message;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GetMessagesInTaskResponseDto extends ResponseDto {
    private Integer taskId;
    private List<MessageDto> messages;

    public GetMessagesInTaskResponseDto(List<Error> errors, Integer taskId, List<MessageDto> messages) {
        super(errors);
        this.taskId = taskId;
        this.messages = messages;
    }

    public static GetMessagesInTaskResponseDto success(Integer taskId, List<Message> messages) {
        return new GetMessagesInTaskResponseDto(Collections.emptyList(), taskId,
                messages.stream().map(DtoBuilder::buildMessageDto).collect(Collectors.toList()));
    }

    public static GetMessagesInTaskResponseDto fail(Integer id, Error error) {
        return new GetMessagesInTaskResponseDto(Collections.singletonList(error), id, Collections.emptyList());
    }

    public static GetMessagesInTaskResponseDto fail(Integer id, List<Error> errors) {
        return new GetMessagesInTaskResponseDto(errors, id, Collections.emptyList());
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }
}
