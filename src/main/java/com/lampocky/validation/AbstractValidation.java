package com.lampocky.validation;

import java.util.List;

public abstract class AbstractValidation<T> {
    protected List<String> errors;

    public abstract boolean validate(T object);
}
