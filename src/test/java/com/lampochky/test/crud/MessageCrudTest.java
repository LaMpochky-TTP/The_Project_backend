package com.lampochky.test.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lampochky.Application;
import com.lampochky.database.entity.*;
import com.lampochky.database.service.*;
import com.lampochky.security.UserSecurity;
import com.lampochky.validation.Error;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageCrudTest extends AbstractCrudTest {

    @Autowired
    public MessageCrudTest(WebApplicationContext context,
                           UserService userService,
                           ProjectService projectService,
                           UserProjectService userProjectService,
                           ListService listService,
                           TaskService taskService,
                           TagService tagService,
                           MessageService messageService) {
        super(context, userService, projectService, userProjectService,
                listService, taskService, tagService, messageService);
    }

    @BeforeAll
    public static void beforeAll(@Autowired UserService userService,
                                 @Autowired ProjectService projectService,
                                 @Autowired UserProjectService userProjectService,
                                 @Autowired ListService listService,
                                 @Autowired TaskService taskService,
                                 @Autowired MessageService messageService) {
        TaskCrudTest.beforeAll(userService, projectService, userProjectService, listService, taskService);
        Integer taskId = 1;
        Task task = taskService.findById(taskId).orElse(null);
        List<User> users = Stream.of("admin@gmail.com", "dev@gmail.com", "guest@gmail.com")
                .map(userService::findByEmail).map(ou -> ou.orElse(null)).collect(Collectors.toList());
        Stream.of(
                new Message("admin message", LocalDateTime.now(), users.get(0), task),
                new Message("dev message", LocalDateTime.now(), users.get(1), task),
                new Message("guest message", LocalDateTime.now(), users.get(2), task)
        ).forEach(messageService::save);
    }

    public static Stream<String> participants(){
        return Stream.of("admin@gmail.com", "dev@gmail.com", "guest@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participants")
    @Order(1)
    @DisplayName("successfully get a message")
    public void getMessage_success(String userEmail) throws Exception{
        final Integer messageId = 1;
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);
        checkIfUserRoleGreaterOrEquals(user, message.getTask().getList().getProject(), UserRole.GUEST);

        mvc.perform(get("/data/message/" + messageId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.id", is(messageId)))
                .andExpect(jsonPath("$.message.id", is(messageId)))
                .andExpect(jsonPath("$.message.text", is(message.getText()) ))
                .andExpect(jsonPath("$.message.taskId", is(message.getTask().getId()) ))
                .andExpect(jsonPath("$.message.user.id", is(message.getUser().getId()) ))
                .andExpect(jsonPath("$.message.dateTime",
                        is(message.getDateTime().format(dateTimeFormatter)) ));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get a message (permissions are not granted)")
    public void getMessage_fail_permissionsNotGranted() throws Exception{
        final Integer messageId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);
        checkIfUserRoleLower(user, message.getTask().getList().getProject(), UserRole.GUEST);

        mvc.perform(get("/data/message/" + messageId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.id", is(messageId)))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get a message (message does not exist)")
    public void getMessage_fail_messageDoesNotExist() throws Exception{
        final Integer messageId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfMessageDoesNotExist(messageId);

        mvc.perform(get("/data/message/" + messageId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.MESSAGE_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.id", is(messageId)))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    @ParameterizedTest
    @MethodSource("participants")
    @Order(1)
    @DisplayName("successfully get all messages in a task")
    public void getMessagesInTask_success(String userEmail) throws Exception{
        final Integer taskId = 1;
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        List<Message> messages = messageService.findAllInTaskSortByDate(task);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.GUEST);

        mvc.perform(get("/data/message/in_task")
                    .param("id", taskId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.taskId", is(taskId)))
                .andExpect(jsonPath("$.messages[*].id",
                        containsInRelativeOrder(messages.stream().map(Message::getId).toArray()) ))
                .andExpect(jsonPath("$.messages[*].text",
                        containsInRelativeOrder(messages.stream().map(Message::getText).toArray()) ))
                .andExpect(jsonPath("$.messages[*].taskId", everyItem(is(taskId)) ))
                .andExpect(jsonPath("$.messages[*].text",
                        containsInRelativeOrder(messages.stream().map(Message::getText).toArray()) ))
                .andExpect(jsonPath("$.messages[*].user.id",
                        containsInRelativeOrder(messages.stream().map(m -> m.getUser().getId()).toArray()) ))
                .andExpect(jsonPath("$.messages[*].dateTime",
                        containsInRelativeOrder(messages.stream()
                                .map(m -> m.getDateTime().format(dateTimeFormatter)).toArray()) ));

    }

    @Test
    @Order(1)
    @DisplayName("failure to get all messages in a task (task does not exist)")
    public void getMessagesInTask_fail_taskDoesNotExist() throws Exception{
        final Integer taskId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTaskDoesNotExist(taskId);

        mvc.perform(get("/data/message/in_task")
                    .param("id", taskId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.TASK_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.taskId", is(taskId)))
                .andExpect(jsonPath("$.messages", emptyIterable()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get all messages in a task (permissions are not granted)")
    public void getMessagesInTask_fail_permissionsNotGranted() throws Exception{
        final Integer taskId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleLower(user, task.getList().getProject(), UserRole.GUEST);

        mvc.perform(get("/data/message/in_task")
                    .param("id", taskId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.taskId", is(taskId)))
                .andExpect(jsonPath("$.messages", emptyIterable()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participants")
    @Order(2)
    @DisplayName("successfully create new message")
    public void createMessage_success(String userEmail) throws Exception{
        final Integer taskId = 1;
        final String text = userEmail + "'s new message";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.GUEST);
        ObjectNode request = mapper.createObjectNode()
                .put("text", text)
                .put("taskId", taskId);

        LocalDateTime beforeRequest = LocalDateTime.now();
        String response = mvc.perform(post("/data/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.message.id", notNullValue()))
                .andExpect(jsonPath("$.message.user.id", is(user.getId()) ))
                .andExpect(jsonPath("$.message.text", is(text)))
                .andExpect(jsonPath("$.message.dateTime", notNullValue()))
                .andReturn().getResponse().getContentAsString();
        LocalDateTime afterRequest = LocalDateTime.now();

        Message message = messageService.findById(mapper.readTree(response).at("/message/id").asInt())
                .orElse(null);

        Assertions.assertNotNull(message);
        Assertions.assertTrue(message.getDateTime().isAfter(beforeRequest));
        Assertions.assertTrue(message.getDateTime().isBefore(afterRequest));
        Assertions.assertEquals(text, message.getText());
        Assertions.assertEquals(user, message.getUser());
    }

    @Test
    @Order(2)
    @DisplayName("failure to create a message (task does not exist)")
    public void createMessage_fail_taskDoesNotExist() throws Exception{
        final Integer taskId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTaskDoesNotExist(taskId);
        ObjectNode request = mapper.createObjectNode()
                .put("text", "some text")
                .put("taskId", taskId);

        mvc.perform(post("/data/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.TASK_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("failure to create a message (permissions are not granted)")
    public void createMessage_fail_permissionsNotGranted() throws Exception{
        final Integer taskId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleLower(user, task.getList().getProject(), UserRole.GUEST);
        ObjectNode request = mapper.createObjectNode()
                .put("text", "some text")
                .put("taskId", taskId);

        mvc.perform(post("/data/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    public static Stream<Arguments> invalidData(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        mapper.createObjectNode()
                                .putNull("text"),
                        Error.MESSAGE_TEXT_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("text", ""),
                        Error.MESSAGE_TEXT_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("text", "too long message text " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890 " +
                                        "1234567890 1234567890 1234567890 1234567890 1234567890"),
                        Error.MESSAGE_TEXT_TOO_LONG
                )
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource("invalidData")
    @Order(2)
    @DisplayName("failure to create a message (invalid data)")
    public void createMessage_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer taskId = 1;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.GUEST);
        request.put("taskId", taskId);

        mvc.perform(post("/data/message")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    public static Stream<Arguments> updateMessage_success(){
        return Stream.of(
                Arguments.of(4, "admin@gmail.com"),
                Arguments.of(5, "dev@gmail.com"),
                Arguments.of(6, "guest@gmail.com")
        );
    }

    @ParameterizedTest(name = "#{index} message id: {0}, user: {1}")
    @MethodSource
    @Order(3)
    @DisplayName("successfully update a message")
    public void updateMessage_success(Integer messageId, String userEmail) throws Exception{
        final String text = userEmail + "'s updated text";
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);
        ObjectNode request = mapper.createObjectNode().put("text", text);

        Assumptions.assumeTrue(message.getUser().equals(user), "user is not owner of a message");

        mvc.perform(put("/data/message/" + messageId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.message.id", is(message.getId()) ))
                .andExpect(jsonPath("$.message.user.id", is(message.getUser().getId()) ))
                .andExpect(jsonPath("$.message.text", is(text)))
                .andExpect(jsonPath("$.message.dateTime",
                        is(message.getDateTime().format(dateTimeFormatter)) ));
    }

    @Test
    @Order(3)
    @DisplayName("failure to update a message (message does not exists)")
    public void updateMessage_fail_messageDoesNotExist() throws Exception{
        final Integer messageId = 10;
        final String userEmail = "admin@gmail.com";
        ObjectNode request = mapper.createObjectNode().put("text", "msg");
        User user = getUserByEmail(userEmail);
        checkIfMessageDoesNotExist(messageId);

        mvc.perform(put("/data/message/" + messageId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.MESSAGE_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));

    }

    public static Stream<String> updateMessage_fail_permissionsNotGranted(){
        return Stream.of("admin@gmail.com", "dev@gmail.com", "nor@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource
    @Order(3)
    @DisplayName("failure to update a message (permissions are not granted)")
    public void updateMessage_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer messageId = 6;
        ObjectNode request = mapper.createObjectNode().put("text", "msg");
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);

        Assumptions.assumeFalse(message.getUser().equals(user), "user is owner of a message");

        mvc.perform(put("/data/message/" + messageId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource("invalidData")
    @Order(3)
    @DisplayName("failure to update a message (invalid data)")
    public void updateMessage_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer messageId = 4;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);

        Assumptions.assumeTrue(message.getUser().equals(user), "user is not owner of a message");

        mvc.perform(put("/data/message/" + messageId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    @Test
    @Order(4)
    @DisplayName("failure to delete a message (message does not exist)")
    public void deleteMessage_fail_messageDoesNotExist() throws Exception{
        final Integer messageId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfMessageDoesNotExist(messageId);

        mvc.perform(delete("/data/message/" + messageId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.MESSAGE_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    public static Stream<String> deleteMessage_fail_permissionsNotGranted() {
        return Stream.of("dev@gmail.com", "nor@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource
    @Order(4)
    @DisplayName("failure to delete a message (message does not exist)")
    public void deleteMessage_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer messageId = 6;
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);
        UserProject relation = userProjectService
                .findByUserAndProject(user, message.getTask().getList().getProject()).orElse(null);

        Assumptions.assumeFalse(
                message.getUser().equals(user) || (relation != null && relation.getConfirmed() &&
                        relation.getRole().greaterOrEquals(UserRole.ADMIN)),
                "user is owner of a message or has admin permissions"
        );

        mvc.perform(delete("/data/message/" + messageId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.message", nullValue()));
    }

    public static Stream<Arguments> deleteMessage_success(){
        return Stream.of(
                Arguments.of(3, "guest@gmail.com"),
                Arguments.of(6, "admin@gmail.com")
        );
    }

    @ParameterizedTest(name = "#{index} message id:{0}, user: {1}")
    @MethodSource
    @Order(5)
    @DisplayName("successfully delete a message")
    public void deleteMessage_success(Integer messageId, String userEmail) throws Exception{
        User user = getUserByEmail(userEmail);
        Message message = getMessageById(messageId);
        UserProject relation = userProjectService
                .findByUserAndProject(user, message.getTask().getList().getProject()).orElse(null);

        Assumptions.assumeTrue(message.getUser().equals(user) || (relation != null && relation.getConfirmed() &&
                        relation.getRole().greaterOrEquals(UserRole.ADMIN)),
                "user is neither the owner of the message nor the admin");

        mvc.perform(delete("/data/message/" + messageId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.message.id", is(message.getId()) ))
                .andExpect(jsonPath("$.message.text", is(message.getText()) ))
                .andExpect(jsonPath("$.message.dateTime",
                        is(message.getDateTime().format(dateTimeFormatter)) ))
                .andExpect(jsonPath("$.message.user.id", is(message.getUser().getId()) ))
                .andExpect(jsonPath("$.message.taskId", is(message.getTask().getId()) ));

        message = messageService.findById(messageId).orElse(null);

        Assertions.assertNull(message);
    }

}
