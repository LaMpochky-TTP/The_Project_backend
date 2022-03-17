package com.lampochky.validation;

import com.lampochky.database.entity.Message;

public class MessageValidator extends AbstractValidator<Message> {

    private void validateText(String text) {
        if(text == null) {
            text = "";
        }
        if(text.isEmpty()) {
            errors.add(Error.MESSAGE_TEXT_EMPTY);
        }
        if(text.length() > 1000) {
            errors.add(Error.MESSAGE_TEXT_TOO_LONG);
        }
    }

    @Override
    public boolean validate(Message message) {
        errors.clear();

        validateText(message.getText());

        return errors.isEmpty();
    }
}
