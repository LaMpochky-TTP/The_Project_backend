package com.lampochky.dto.response.message;

import com.lampochky.database.entity.Message;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class MessageResponseDto extends ResponseDto {
    private MessageDto message;

    public MessageResponseDto(List<Error> errors, MessageDto message) {
        super(errors);
        this.message = message;
    }

    public static MessageResponseDto success(Message message) {
        return new MessageResponseDto(Collections.emptyList(), DtoBuilder.buildMessageDto(message));
    }

    public static MessageResponseDto fail(Error error) {
        return new MessageResponseDto(Collections.singletonList(error), null);
    }

    public static MessageResponseDto fail(List<Error> errors){
        return new MessageResponseDto(errors, null);
    }

    public MessageDto getMessage() {
        return message;
    }

    public void setMessage(MessageDto message) {
        this.message = message;
    }
}
