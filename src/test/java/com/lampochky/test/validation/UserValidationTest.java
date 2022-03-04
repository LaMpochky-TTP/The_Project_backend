package com.lampochky.test.validation;

import com.lampocky.Application;
import com.lampocky.database.entity.User;
import com.lampocky.database.service.UserService;
import com.lampocky.dto.request.RegisterRequestDto;
import com.lampocky.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(classes = Application.class)
public class UserValidationTest {
    private final UserValidation userValidation;

    @Autowired
    public UserValidationTest(UserService userService){
        userValidation = new UserValidation(userService);
    }

    @BeforeAll
    public static void beforeAll (@Autowired UserService userService) {
        for(int i = 1; i <= 3; ++i){
            User user = new User("uv_name_" + i, "Pass1", "uv_email_" + i + "@gmail.com");
            userService.save(user);
        }
    }

    public static RegisterRequestDto convertToRequests(User user) {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername(user.getUsername());
        request.setEmail(user.getEmail());
        request.setPassword(user.getPassword());
        return request;
    }

    public static Stream<User> userValidation_success() {
        return Stream.of(
                new User("Petia", "Password1", "email@gmail.com"),
                new User("agent007", "$upperPassword12345", "agent@knu.ua"),
                new User("--101--", "EZpwd0", "aaa.bbb@gmail.com"),
                new User("dragon_hunter", "D1ff!cult#Pa$$w0rd", "hunter@ukr.net")
        );
    }

    private void check(User user, String error) {
        Assertions.assertFalse(userValidation.validate(user));
        Assertions.assertEquals(1, userValidation.getErrors().size());
        Assertions.assertTrue(userValidation.getErrors().get(0).equals(error));
    }

    private void check(RegisterRequestDto request, String error){
        Assertions.assertFalse(userValidation.validate(request));
        Assertions.assertEquals(1, userValidation.getErrors().size());
        Assertions.assertTrue(userValidation.getErrors().get(0).equals(error));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("correct user validation")
    public void userValidation_success(User user) {
        Assertions.assertTrue(userValidation.validate(user));
        Assertions.assertEquals(0, userValidation.getErrors().size());
    }

    public static Stream<RegisterRequestDto> requestValidation_success() {
        return userValidation_success().map(UserValidationTest::convertToRequests);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Correct request validation")
    public void requestValidation_success(RegisterRequestDto requestDto) {
        Assertions.assertTrue(userValidation.validate(requestDto));
        Assertions.assertEquals(0, userValidation.getErrors().size());
    }

    public static Stream<Arguments> wrongUserUsername_fail() {
        return Stream.of(
                Arguments.of(
                        new User(null, "Correct0pwd", "corect_email@gmail.com"),
                        "username is empty"
                ),
                Arguments.of(
                        new User("", "Correct0pwd", "corect_email@gmail.com"),
                        "username is empty"
                ),
                Arguments.of(
                        new User("uv_name_1", "Correct0pwd", "corect_email@gmail.com"),
                        "username is already in use"
                ),
                Arguments.of(
                        new User("Too_long_username_without_illegal_characters", "Correct0pwd", "corect_email@gmail.com"),
                        "username must not be longer 20 characters"
                ),
                Arguments.of(
                        new User("illegal character$", "Correct0pwd", "corect_email@gmail.com"),
                        "username must contain latin letters, digits, _, - only"
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Wrong user's username")
    public void wrongUserUsername_fail(User user, String error) {
        check(user, error);
    }

    public static Stream<Arguments> wrongRequestUsername_fail() {
        return wrongUserUsername_fail()
                .map(arguments -> Arguments.of(
                        convertToRequests((User)arguments.get()[0]),
                        arguments.get()[1]
                ));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Wrong username in request")
    public void wrongRequestUsername_fail(RegisterRequestDto request, String error) {
        check(request, error);
    }

    public static Stream<Arguments> wrongUserEmail_fail() {
        return Stream.of(
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", null),
                        "email is empty"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", ""),
                        "email is empty"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", "uv_email_1@gmail.com"),
                        "email is already in use"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", "incorrect_email"),
                        "email is incorrect"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", "no_host@"),
                        "email is incorrect"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", "illegal_host@host"),
                        "email is incorrect"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", "illegal symbols @ host.ua"),
                        "email is incorrect"
                ),
                Arguments.of(
                        new User("Correct_name", "Correct0pwd", "@no_name.com"),
                        "email is incorrect"
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Wrong user's email")
    public void wrongUserEmail_fail(User user, String error) {
        check(user, error);
    }

    public static Stream<Arguments> wrongRequestEmail_fail(){
        return wrongUserEmail_fail()
                .map(arguments -> Arguments.of(
                        convertToRequests((User)arguments.get()[0]),
                        arguments.get()[1]
                ));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Wrong email in request")
    public void wrongRequestEmail_fail(RegisterRequestDto request, String error) {
        check(request, error);
    }

    public static Stream<Arguments> wrongUserPassword_fail(){
        return Stream.of(
                Arguments.of(
                        new User("correct_name", "Too long password 1 with all required characters", "correct@gmail.com"),
                        "password must not be longer then 20 characters"
                ),
                Arguments.of(
                        new User("correct_name", "no uppercase 1", "correct@gmail.com"),
                        "password must contain uppercased latin letters"
                ),
                Arguments.of(
                        new User("correct_name", "NO LOWERCASE 1", "correct@gmail.com"),
                        "password must contain lowercased latin letters"
                ),
                Arguments.of(
                        new User("correct_name", "No digits", "correct@gmail.com"),
                        "password must contain digits"
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Wrong user's password")
    public void wrongUserPassword_fail(User user, String error) {
        check(user, error);
    }

    public static Stream<Arguments> wrongRequestPassword_fail(){
        return wrongUserPassword_fail()
                .map(arguments -> Arguments.of(
                        convertToRequests((User)arguments.get()[0]),
                        arguments.get()[1]
                ));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Wrong password in request")
    public void wrongRequestPassword_fail(RegisterRequestDto request, String error) {
        check(request, error);
    }

    public static Stream<Arguments> multipleErrorsUserValidation_fail(){
        return Stream.of(
                Arguments.of(
                        new User("correct_name", null, "correct@gmail.com"),
                        Arrays.asList(
                                "password must be longer then 5 characters",
                                "password must contain digits",
                                "password must contain uppercased latin letters",
                                "password must contain lowercased latin letters"
                        )
                ),
                Arguments.of(
                        new User("illegal character$", "Correct1", "no_host@"),
                        Arrays.asList(
                                "username must contain latin letters, digits, _, - only",
                                "email is incorrect"
                        )
                ),
                Arguments.of(
                        new User(null, "", null),
                        Arrays.asList(
                                "password must be longer then 5 characters",
                                "password must contain digits",
                                "password must contain uppercased latin letters",
                                "password must contain lowercased latin letters",
                                "email is empty",
                                "username is empty"
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Multiple user errors")
    public void multipleErrorsUserValidation_fail(User user, List<String> errors){
        Assertions.assertFalse(userValidation.validate(user));
        Assertions.assertEquals(errors.size(), userValidation.getErrors().size());
        Assertions.assertTrue(userValidation.getErrors().containsAll(errors));
    }

    public static Stream<Arguments> multipleErrorsRequestValidation_fail(){
        return multipleErrorsUserValidation_fail()
                .map(arguments -> Arguments.of(
                        convertToRequests((User)arguments.get()[0]),
                        arguments.get()[1]
                ));
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Multiple errors in request")
    public void multipleErrorsRequestValidation_fail(RegisterRequestDto request, List<String> errors){
        Assertions.assertFalse(userValidation.validate(request));
        Assertions.assertEquals(errors.size(), userValidation.getErrors().size());
        Assertions.assertTrue(userValidation.getErrors().containsAll(errors));
    }
}
