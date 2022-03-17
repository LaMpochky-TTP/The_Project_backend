package com.lampochky.validation;

import com.lampochky.database.entity.User;
import com.lampochky.database.service.UserService;
import com.lampochky.dto.request.auth.RegisterRequestDto;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserValidator extends AbstractValidator<User> {
    private final Pattern passwordNumbersPattern = Pattern.compile(".*[0-9].*");
    private final Pattern passwordUpperLettersPattern = Pattern.compile(".*[A-Z].*");
    private final Pattern passwordLowerLettersPattern = Pattern.compile(".*[a-z].*");
    private final Pattern emailPattern = Pattern.compile("\\S+@\\S+\\.\\S+");
    private final UserService userService;

    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    private void validateUsername(String username){
        if(username == null){
            username = "";
        }
        if(username.isEmpty()) {
            errors.add(Error.USERNAME_EMPTY);
        }
        if(username.length() > 20) {
            errors.add(Error.USERNAME_TOO_LONG);
        }
        if(!namePattern.matcher(username).matches()){
            errors.add(Error.USERNAME_ILLEGAL_CHARACTERS);
        }
        Optional<User> optionalUser = userService.findByUsername(username);
        if(optionalUser.isPresent()) {
            errors.add(Error.USERNAME_IN_USE);
        }
    }

    private void validatePassword(String password) {
        if(password == null){
            password = "";
        }
        if(password.length() < 5) {
            errors.add(Error.PASSWORD_TOO_SHORT);
        }
        if(password.length() > 20){
            errors.add(Error.PASSWORD_TOO_LONG);
        }
        if(!passwordNumbersPattern.matcher(password).matches()){
            errors.add(Error.PASSWORD_NO_DIGITS);
        }
        if(!passwordUpperLettersPattern.matcher(password).matches()){
            errors.add(Error.PASSWORD_NO_UPPERCASE);
        }
        if(!passwordLowerLettersPattern.matcher(password).matches()){
            errors.add(Error.PASSWORD_NO_LOWERCASE);
        }
    }

    private void validateEmail(String email) {
        if(email == null){
            email = "";
        }
        if(email.isEmpty()) {
            errors.add(Error.EMAIL_EMPTY);
        } else if(!emailPattern.matcher(email).matches()){
            errors.add(Error.EMAIL_ILLEGAL);
        }
        Optional<User> optionalUser = userService.findByEmail(email);
        if(optionalUser.isPresent()) {
            errors.add(Error.EMAIL_IN_USE);
        }
    }

    public boolean validate(RegisterRequestDto request){
        errors.clear();
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());
        validateEmail(request.getEmail());
        return errors.isEmpty();
    }

    @Override
    public boolean validate(User user) {
        errors.clear();
        validateUsername(user.getUsername());
        validatePassword(user.getPassword());
        validateEmail(user.getEmail());
        return errors.isEmpty();
    }

    public List<Error> getErrors() {
        return errors;
    }
}
