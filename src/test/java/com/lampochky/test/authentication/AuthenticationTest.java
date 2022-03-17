package com.lampochky.test.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lampochky.Application;
import com.lampochky.database.entity.User;
import com.lampochky.database.service.UserService;
import com.lampochky.security.JwtTokenProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class AuthenticationTest {
    private final MockMvc mockMvc;
    private final UserService userService;
    private final JwtTokenProvider provider;
    private final ObjectMapper mapper;

    @Autowired
    public AuthenticationTest(MockMvc mockMvc, UserService userService, JwtTokenProvider provider) {
        this.mockMvc = mockMvc;
        this.userService = userService;
        this.provider = provider;
        mapper = new ObjectMapper();
    }

    private String getNullableText(JsonNode node) {
        if(node.isNull()){
            return null;
        } else {
            return node.asText();
        }
    }

    @BeforeAll
    public static void beforeAll(@Autowired UserService userService){
        userService.save(new User("test_name", "test_pwd", "test_email@gmail.com"));
    }

    @Test
    @Tag("login")
    @DisplayName("Common login")
    public void login_success() throws Exception{
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("email", "test_email@gmail.com");
        jsonRequestBody.put("password", "test_pwd");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test_email@gmail.com")))
                .andExpect(jsonPath("$.username", is("test_name")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andReturn().getResponse().getContentAsString();

        String token = getNullableText(mapper.readTree(response).at("/token"));
        Assertions.assertNotNull(token);
        Assertions.assertTrue(provider.isValid(token));
        Assertions.assertEquals("test_email@gmail.com", provider.getEmail(token));
    }

    @Test
    @Tag("login")
    @DisplayName("Login with external fields")
    public void loginWithExternalFields_success() throws Exception{
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("email", "test_email@gmail.com");
        jsonRequestBody.put("password", "test_pwd");
        jsonRequestBody.putNull("external_field");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test_email@gmail.com")))
                .andExpect(jsonPath("$.username", is("test_name")))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.error", nullValue()))
                .andReturn().getResponse().getContentAsString();

        String token = getNullableText(mapper.readTree(response).at("/token"));
        Assertions.assertNotNull(token);
        Assertions.assertTrue(provider.isValid(token));
        Assertions.assertEquals("test_email@gmail.com", provider.getEmail(token));
    }

    @Test
    @Tag("login")
    @DisplayName("Login with wrong password")
    public void loginWrongPassword_fail() throws Exception {
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("email", "test_email@gmail.com");
        jsonRequestBody.put("password", "wrong password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test_email@gmail.com")))
                .andExpect(jsonPath("$.username", nullValue()))
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", not(blankOrNullString())));
    }

    @Test
    @Tag("login")
    @DisplayName("Login without password")
    public void loginNoPassword_fail() throws Exception {
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("email", "test_email@gmail.com");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test_email@gmail.com")))
                .andExpect(jsonPath("$.username", nullValue()))
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", not(blankOrNullString())));
    }

    @Test
    @Tag("login")
    @DisplayName("Login with wrong email")
    public void loginWrongEmail_fail() throws Exception {
        String wrongEmail = "wrong_email";
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("email", wrongEmail);
        jsonRequestBody.put("password", "test_pwd");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(wrongEmail)))
                .andExpect(jsonPath("$.username", nullValue()))
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", not(blankOrNullString())));
    }

    @Test
    @Tag("login")
    @DisplayName("Login without email")
    public void loginNoEmail_fail() throws Exception {
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("password", "test_pwd");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", nullValue()))
                .andExpect(jsonPath("$.username", nullValue()))
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error", not(blankOrNullString())));
    }

    @Test
    @Tag("registration")
    @DisplayName("Common registration")
    public void registration_success() throws Exception {
        String username = "new_user", email = "new_email@gmail.com", password = "Pass1";
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("username", username);
        jsonRequestBody.put("email", email);
        jsonRequestBody.put("password", password);

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andReturn().getResponse().getContentAsString();

        String token = getNullableText(mapper.readTree(response).at("/token"));
        Assertions.assertNotNull(token);
        Assertions.assertTrue(provider.isValid(token));
        Assertions.assertEquals(email, provider.getEmail(token));

        userService.delete(userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found")));
    }

    @Test
    @Tag("registration")
    @DisplayName("Registration with external fields")
    public void registrationWithExternalFields_success() throws Exception {
        String username = "new_user", email = "new_email@gmail.com", password = "Pass1";
        ObjectNode jsonRequestBody = mapper.createObjectNode();
        jsonRequestBody.put("username", username);
        jsonRequestBody.put("email", email);
        jsonRequestBody.put("password", password);
        jsonRequestBody.putNull("external_field");

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andReturn().getResponse().getContentAsString();

        String token = getNullableText(mapper.readTree(response).at("/token"));
        Assertions.assertNotNull(token);
        Assertions.assertTrue(provider.isValid(token));
        Assertions.assertEquals(email, provider.getEmail(token));

        userService.delete(userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found")));
    }

    public static Stream<ObjectNode> registrationWithoutRequiredFields_fail() {
        String username = "new_user", email = "new_email@gmail.com", password = "Pass1";
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                mapper.createObjectNode()
                    .put("email", email)
                    .put("password", password),
                mapper.createObjectNode()
                    .put("username", username)
                    .put("password", password),
                mapper.createObjectNode()
                    .put("username", username)
                    .put("email", email)
        );
    }

    @ParameterizedTest
    @MethodSource
    @Tag("registration")
    @DisplayName("registration without required fields")
    public void registrationWithoutRequiredFields_fail(ObjectNode jsonRequestBody) throws Exception{
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", not(emptyIterable())));
    }

    public static Stream<Arguments> registrationWithWrongFieldsValues_fail(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                    mapper.createObjectNode()
                        .put("username", "some_name")
                        .put("email", "wrong_email")
                        .put("password", "Pass1"),
                    1
                ),
                Arguments.of(
                    mapper.createObjectNode()
                        .put("username", "test_name")
                        .put("email", "test_email@gmail.com")
                        .put("password", "Pass1"),
                    2
                ),
                Arguments.of(
                    mapper.createObjectNode()
                        .putNull("username")
                        .put("email", "some_email@gmail.com")
                        .put("password", "password"),
                    3
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    @Tag("registration")
    @DisplayName("registration with wrong fields values")
    public void registrationWithWrongFieldsValues_fail(ObjectNode jsonRequestBody, int errorCount) throws Exception{
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestBody.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email",
                        is(getNullableText(jsonRequestBody.get("email"))) ))
                .andExpect(jsonPath("$.username",
                        is(getNullableText(jsonRequestBody.get("username"))) ))
                .andExpect(jsonPath("$.token", nullValue()))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", iterableWithSize(errorCount)));
    }

    @Test
    @Tag("authorization")
    @DisplayName("Common authorization via jwt")
    public void jwtAuthorization_success() throws Exception {
        String token = provider.createToken("test_email@gmail.com");
        Assumptions.assumeTrue(token != null && !token.isEmpty(), "Token is null or empty");
        mockMvc.perform(get("/auth/login-test")
                        .header("Authorization", "Bearer_" + token))
                .andExpect(status().isOk());

    }

    @Test
    @Tag("authorization")
    @DisplayName("Authorization without 'Authorization' header")
    public void jwtAuthNoHeader_fail() throws Exception {
        mockMvc.perform(get("/auth/login-test"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Tag("authorization")
    @DisplayName("Authorization with expired token")
    public void jwtAuthExpiredToken_fail() throws Exception {
        int fiveMinutesInMillis = 1000 * 60 * 5;
        Date now = new Date();
        Date from = new Date(now.getTime() - fiveMinutesInMillis * 2);
        Date to = new Date(now.getTime() - fiveMinutesInMillis);
        String token = provider.createToken("test_email@gmail.com", from, to);

        mockMvc.perform(get("/auth/login-test")
                        .header("Authorization", "Bearer_" + token))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Tag("authorization")
    @DisplayName("Authorization with wrong email in token")
    public void jwtAuthWrongEmail_fail() throws Exception {
        String token = provider.createToken("wrong_email@gmail.com");

        mockMvc.perform(get("/auth/login-test")
                        .header("Authorization", "Bearer_" + token))
                .andExpect(status().is4xxClientError());
    }

}
