package com.lampochky.validation;

import com.lampochky.database.entity.Tag;

public class TagValidator extends AbstractValidator<Tag>{

    @Override
    public boolean validate(Tag tag) {
        errors.clear();

        validateName(tag.getName());

        return errors.isEmpty();
    }
}
