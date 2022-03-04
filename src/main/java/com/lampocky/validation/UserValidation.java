package com.lampocky.validation;

import com.lampocky.database.entity.User;
import com.lampocky.database.service.UserService;
import com.lampocky.dto.request.RegisterRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserValidation {
    private List<String> errors = new ArrayList<>();
    private Pattern usernamePattern = Pattern.compile("[a-zA-Z0-9_\\-]*");
    private Pattern passwordNumbersPattern = Pattern.compile(".*[0-9].*");
    private Pattern passwordUpperLettersPattern = Pattern.compile(".*[A-Z].*");
    private Pattern passwordLowerLettersPattern = Pattern.compile(".*[a-z].*");
    private Pattern emailPattern = Pattern.compile("\\S+@\\S+\\.\\S+");
    private UserService userService;

    public UserValidation(UserService userService) {
        this.userService = userService;
    }

    private void validateUsername(String username){
        if(username == null){
            username = "";
        }
        if(username.isEmpty()) {
            errors.add("username is empty");
        }
        if(username.length() > 20) {
            errors.add("username must not be longer 20 characters");
        }
        if(!usernamePattern.matcher(username).matches()){
            errors.add("username must contain latin letters, digits, _, - only");
        }
        Optional<User> optionalUser = userService.findByUsername(username);
        if(optionalUser.isPresent()) {
            errors.add("username is already in use");
        }
    }

    private void validatePassword(String password) {
        if(password == null){
            password = "";
        }
        if(password.length() < 5) {
            errors.add("password must be longer then 5 characters");
        }
        if(password.length() > 20){
            errors.add("password must not be longer then 20 characters");
        }
        if(!passwordNumbersPattern.matcher(password).matches()){
            errors.add("password must contain digits");
        }
        if(!passwordUpperLettersPattern.matcher(password).matches()){
            errors.add("password must contain uppercased latin letters");
        }
        if(!passwordLowerLettersPattern.matcher(password).matches()){
            errors.add("password must contain lowercased latin letters");
        }
    }

    private void validateEmail(String email) {
        if(email == null){
            email = "";
        }
        if(email.isEmpty()) {
            errors.add("email is empty");
        } else if(!emailPattern.matcher(email).matches()){
            errors.add("email is incorrect");
        }
        Optional<User> optionalUser = userService.findByEmail(email);
        if(optionalUser.isPresent()) {
            errors.add("email is already in use");
        }
    }

    public boolean validate(RegisterRequestDto request){
        errors.clear();
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());
        validateEmail(request.getEmail());
        return errors.isEmpty();
    }

    public boolean validate(User user) {
        errors.clear();
        validateUsername(user.getUsername());
        validatePassword(user.getPassword());
        validateEmail(user.getEmail());
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }
}
