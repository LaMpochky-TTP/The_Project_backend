package com.lampochky.validation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lampochky.database.entity.UserRole;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Error {
    LOGIN_FAIL("wrong email or password"),
    AUTHENTICATION_FAIL("authentication failed"),
    PERMISSIONS_NOT_GRANTED("permissions to action not granted"),
    USER_NOT_FOUND("user not found"),

    PROJECT_NOT_FOUND("project not found"),
    USER_IS_ALREADY_MEMBER("user is already member of project"),
    INVITION_NOT_FOUND("invition not found"),

    LIST_NOT_FOUND("list not found"),

    TASK_NOT_FOUND("task not found"),

    TAG_NOT_FOUND("tag not found"),

    MESSAGE_NOT_FOUND("message not found"),
    MESSAGE_TEXT_EMPTY("text must not be empty"),
    MESSAGE_TEXT_TOO_LONG("text must not be longer then 1000 characters"),

    DATE_ORDER_INVALID("date to start must not be after date to finish"),
    ASSIGNING_NOT_GRANTED_USER("assigned user must have role " + UserRole.DEVELOPER.name() + " or higher"),

    NAME_EMPTY("name is empty"),
    NAME_TOO_LONG("name must not be longer 20 characters"),
    NAME_ILLEGAL_CHARACTERS("name must contain latin letters, digits, _, - only"),

    USERNAME_EMPTY("username is empty"),
    USERNAME_TOO_LONG("username must not be longer 20 characters"),
    USERNAME_ILLEGAL_CHARACTERS("username must contain latin letters, digits, _, - only"),
    USERNAME_IN_USE("username is already in use"),

    PASSWORD_TOO_SHORT("password must be longer then 5 characters"),
    PASSWORD_TOO_LONG("password must not be longer then 20 characters"),
    PASSWORD_NO_DIGITS("password must contain digits"),
    PASSWORD_NO_UPPERCASE("password must contain uppercased latin letters"),
    PASSWORD_NO_LOWERCASE("password must contain lowercased latin letters"),

    EMAIL_EMPTY("email is empty"),
    EMAIL_ILLEGAL("email is incorrect"),
    EMAIL_IN_USE("email is already in use")
    ;
    @JsonProperty
    private final String errName;

    @JsonProperty
    private String message;

    Error(String message) {
        errName = name();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
