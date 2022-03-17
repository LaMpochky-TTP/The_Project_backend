package com.lampochky.dto.response.message;

import com.lampochky.database.entity.Message;
import com.lampochky.dto.response.DtoBuilder;
import com.lampochky.dto.response.ResponseDto;
import com.lampochky.validation.Error;

import java.util.Collections;
import java.util.List;

public class GetMessageByIdResponseDto extends ResponseDto {
    private Integer id;
    private MessageDto message;

    private GetMessageByIdResponseDto(List<Error> errors, Integer id, MessageDto message) {
        super(errors);
        this.id = id;
        this.message = message;
    }

    public static GetMessageByIdResponseDto success(Message message){
        return new GetMessageByIdResponseDto(Collections.emptyList(), message.getId(),
                DtoBuilder.buildMessageDto(message));
    }

    public static GetMessageByIdResponseDto fail(Integer id, Error error){
        return  new GetMessageByIdResponseDto(Collections.singletonList(error), id, null);
    }

    public static GetMessageByIdResponseDto fail(Integer id, List<Error> errors){
        return  new GetMessageByIdResponseDto(errors, id, null);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MessageDto getMessage() {
        return message;
    }

    public void setMessage(MessageDto message) {
        this.message = message;
    }
}
