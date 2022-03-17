package com.lampochky.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractValidator<T> {
    protected final List<Error> errors;
    protected final Pattern namePattern = Pattern.compile("[a-zA-Z0-9_\\-]*");

    public AbstractValidator(){
        errors = new ArrayList<>();
    }

    protected void validateName(String name){
        if(name == null){
            name = "";
        }
        if(name.isEmpty()) {
            errors.add(Error.NAME_EMPTY);
            System.out.println("Empty");
        }
        if(name.length() > 20) {
            errors.add(Error.NAME_TOO_LONG);
            System.out.println("Too long");
        }
        if(!namePattern.matcher(name).matches()){
            errors.add(Error.NAME_ILLEGAL_CHARACTERS);
            System.out.println("Illegal chars");
        }
    }

    public abstract boolean validate(T entity);

    public List<Error> getErrors() {
        return errors;
    }
}
