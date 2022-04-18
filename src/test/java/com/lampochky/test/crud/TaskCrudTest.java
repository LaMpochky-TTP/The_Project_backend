package com.lampochky.test.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lampochky.Application;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.database.entity.Task;
import com.lampochky.database.entity.TaskList;
import com.lampochky.database.entity.User;
import com.lampochky.database.entity.UserRole;
import com.lampochky.database.service.*;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
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
public class TaskCrudTest extends AbstractCrudTest {
    @Autowired
    public TaskCrudTest(WebApplicationContext context,
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
                                 @Autowired TaskService taskService){
        ListCrudTest.beforeAll(userService, projectService, userProjectService, listService);
        User admin = userService.findByEmail("admin@gmail.com").orElse(null);
        User dev = userService.findByEmail("dev@gmail.com").orElse(null);
        TaskList list = listService.findById(1).orElse(null);
        Stream.of(
                new Task(null, "task_1", LocalDate.now(), LocalDate.now().plusDays(15),
                        1, "desc 1", list, dev, admin),
                new Task(null, "task_2", LocalDate.now().minusDays(7), LocalDate.now().plusDays(7),
                        2, "desc 2", list, null, admin),
                new Task(null, "task_3", LocalDate.now().minusDays(10), LocalDate.now().minusDays(2),
                        3, "desc 3", list, dev, dev)
        ).forEach(taskService::save);
    }

    protected void testTaskDto(ResultActions resultActions, Task task) throws Exception {
        resultActions
                .andExpect(jsonPath("$.task.name", is(task.getName()) ))
                .andExpect(jsonPath("$.task.dateToStart",
                        is(task.getDateToStart().format(dateFormat)) ))
                .andExpect(jsonPath("$.task.dateToFinish",
                        is(task.getDateToFinish().format(dateFormat)) ))
                .andExpect(jsonPath("$.task.priority", is(task.getPriority()) ))
                .andExpect(jsonPath("$.task.description", is(task.getDescription()) ))
                .andExpect(jsonPath("$.task.listId", is(task.getList().getId()) ))
                .andExpectAll(
                        jsonPath("$.task.creator.id", is(task.getCreator().getId()) ),
                        jsonPath("$.task.creator.username", is(task.getCreator().getUsername()) ),
                        jsonPath("$.task.creator.email", is(task.getCreator().getEmail()) ));

        if(task.getAssignedUser() == null){
            resultActions.andExpect(jsonPath("$.task.assignedUser", nullValue()));
        } else {
            resultActions.andExpectAll(
                    jsonPath("$.task.assignedUser.id", is(task.getAssignedUser().getId()) ),
                    jsonPath("$.task.assignedUser.username", is(task.getAssignedUser().getUsername()) ),
                    jsonPath("$.task.assignedUser.email", is(task.getAssignedUser().getEmail()) ));
        }
    }

    protected void testListOfTaskDtos(ResultActions resultActions, List<Task> tasks) throws Exception{
        resultActions
                .andExpect(jsonPath("$.tasks[*].id",
                        containsInAnyOrder(tasks.stream().map(Task::getId).toArray()) ))
                .andExpect(jsonPath("$.tasks[*].name",
                        containsInAnyOrder(tasks.stream().map(Task::getName).toArray()) ))
                .andExpect(jsonPath("$.tasks[*].dateToStart",
                        containsInAnyOrder(tasks.stream().map(t -> t.getDateToStart().format(dateFormat)).toArray()) ))
                .andExpect(jsonPath("$.tasks[*].dateToFinish",
                        containsInAnyOrder(tasks.stream().map(t -> t.getDateToFinish().format(dateFormat)).toArray()) ))
                .andExpect(jsonPath("$.tasks[*].priority",
                        containsInAnyOrder(tasks.stream().map(Task::getPriority).toArray()) ))
                .andExpect(jsonPath("$.tasks[*].description",
                        containsInAnyOrder(tasks.stream().map(Task::getDescription).toArray()) ))
                .andExpect(jsonPath("$.tasks[*].listId",
                        containsInAnyOrder(tasks.stream().map(t -> t.getList().getId()).toArray()) ))
                .andExpectAll(
                        jsonPath("$.tasks[*].creator.id",
                                containsInAnyOrder(tasks.stream().map(t -> t.getCreator().getId()).toArray()) ),
                        jsonPath("$.tasks[*].creator.username",
                                containsInAnyOrder(tasks.stream().map(t -> t.getCreator().getUsername()).toArray()) ),
                        jsonPath("$.tasks[*].creator.email",
                                containsInAnyOrder(tasks.stream().map(t -> t.getCreator().getEmail()).toArray()) ));

        for(int i = 0; i < tasks.size(); ++i){
            User assignedUser = tasks.get(i).getAssignedUser();
            if(assignedUser == null){
                resultActions.andExpect(jsonPath("$.tasks[" + i + "].assignedUser", nullValue()));
            } else {
                resultActions.andExpectAll(
                        jsonPath("$.tasks[" + i + "].assignedUser.id", is(assignedUser.getId()) ),
                        jsonPath("$.tasks[" + i + "].assignedUser.username", is(assignedUser.getUsername()) ),
                        jsonPath("$.tasks[" + i + "].assignedUser.email", is(assignedUser.getEmail()) ));
            }
        }
    }

    public static Stream<String> participants() {
        return Stream.of("admin@gmail.com", "dev@gmail.com", "guest@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participants")
    @Order(1)
    @DisplayName("successfully get task")
    public void getTask_success(String userEmail) throws Exception{
        final Integer taskId = 1;
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.GUEST);

        ResultActions resultActions = mvc.perform(get("/data/task/" + taskId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.task.id", is(taskId) ));
        testTaskDto(resultActions, task);
    }

    @Test
    @Order(1)
    @DisplayName("failure to get a task (permissions are not granted)")
    public void getTask_fail_permissionsAreNotGranted() throws Exception{
        final Integer taskId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleLower(user, task.getList().getProject(), UserRole.GUEST);

        mvc.perform(get("/data/task/" + taskId)
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get a task (task does not exist)")
    public void getTask_fail_taskDoesNotExist() throws Exception{
        final Integer taskId = 10;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTaskDoesNotExist(taskId);

        mvc.perform(get("/data/task/" + taskId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.TASK_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participants")
    @Order(1)
    @DisplayName("successfully get all tasks in the list")
    public void getAllTasksInTheList_success(String userEmail) throws Exception{
        final Integer listId = 1;
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        List<Task> tasks = taskList.getTasks();
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.GUEST);

        ResultActions resultActions = mvc.perform(get("/data/task/in_list")
                    .param("id", listId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.listId", is(listId)));
        testListOfTaskDtos(resultActions, tasks);
    }

    @Test
    @Order(1)
    @DisplayName("failure to get all tasks in the list (permissions are not granted)")
    public void getAllTasksInTheList_fail_permissionsNotGranted() throws Exception{
        final Integer listId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleLower(user, taskList.getProject(), UserRole.GUEST);

        mvc.perform(get("/data/task/in_list")
                    .param("id", listId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("$.listId", is(listId)))
                .andExpect(jsonPath("$.tasks", emptyIterable()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get all tasks in the list (list does not exist)")
    public void getAllTasksInTheList_fail_listDoesNotExist() throws Exception{
        final Integer listId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfListDoesNotExist(listId);

        mvc.perform(get("/data/task/in_list")
                    .param("id", listId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.LIST_NOT_FOUND.name())))
                .andExpect(jsonPath("$.listId", is(listId)))
                .andExpect(jsonPath("$.tasks", emptyIterable()));
    }

    protected static ObjectNode buildValidTaskRequest(ObjectMapper mapper){
        ObjectNode node = mapper.createObjectNode()
                .put("name", "new_task")
                .put("dateToStart", LocalDate.now().minusDays(1).format(dateFormat))
                .put("dateToFinish", LocalDate.now().plusDays(15).format(dateFormat))
                .put("priority", 1)
                .put("description", "new task description")
                .put("listId", 1)
                .putNull("assignedUserId");
        node.putArray("tagIds");
        return node;
    }

    public static Task buildTaskFromNode(ObjectNode request) {
        Task task = new Task();
        task.setName(request.get("name").asText());
        task.setDateToStart(LocalDate.parse(request.get("dateToStart").asText(), dateFormat));
        task.setDateToFinish(LocalDate.parse(request.get("dateToFinish").asText(), dateFormat));
        task.setPriority(request.get("priority").asInt());
        task.setDescription(request.get("description").asText());
        return task;
    }

    public static Stream<Arguments> createTask_success(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        buildValidTaskRequest(mapper),
                        "admin@gmail.com"
                ),
                Arguments.of(
                        buildValidTaskRequest(mapper).put("assignedUserId", 2),
                        "dev@gmail.com"
                )
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource
    @Order(2)
    @DisplayName("successfully create a task")
    public void createTask_success(ObjectNode request, String userEmail) throws Exception{
        final Integer listId = request.get("listId").asInt();
        Task task = buildTaskFromNode(request);
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        task.setCreator(user);
        task.setList(taskList);
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.DEVELOPER);

        if(!request.get("assignedUserId").isNull()) {
            User assignedUser = getUserById(request.get("assignedUserId").asInt());
            task.setAssignedUser(assignedUser);
        }

        ResultActions resultActions = mvc.perform(post("/data/task/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.task.id", notNullValue() ))
                .andExpect(jsonPath("$.task.messages", emptyIterable()));
        testTaskDto(resultActions, task);

        task = taskService.findById(mapper.readTree(resultActions.andReturn().getResponse().getContentAsString())
                .at("/task/id").asInt()).orElse(null);

        Assertions.assertNotNull(task);
    }

    public static Stream<String> notDevs() {
        return Stream.of("guest@gmail.com", "nor@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(2)
    @DisplayName("failure to create a task (permissions are not granted)")
    public void createTask_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer listId = 1;
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleLower(user, taskList.getProject(), UserRole.DEVELOPER);
        ObjectNode request = buildValidTaskRequest(mapper).put("listId", listId);

        mvc.perform(post("/data/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("$.task", nullValue() ));
    }

    public static Stream<Arguments> invalidData() {
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        buildValidTaskRequest(mapper).put("name", ""),
                        Error.NAME_EMPTY
                ),
                Arguments.of(
                        buildValidTaskRequest(mapper).put("name", "too_long_name_of_the_task"),
                        Error.NAME_TOO_LONG
                ),
                Arguments.of(
                        buildValidTaskRequest(mapper).put("name", "illegal character$"),
                        Error.NAME_ILLEGAL_CHARACTERS
                ),
                Arguments.of(
                        buildValidTaskRequest(mapper)
                                .put("dateToStart", LocalDate.now().plusDays(5).format(dateFormat))
                                .put("dateToFinish", LocalDate.now().minusDays(5).format(dateFormat)),
                        Error.DATE_ORDER_INVALID
                )
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource("invalidData")
    @Order(2)
    @DisplayName("failure to create a task (invalid data)")
    public void createTask_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer listId = request.get("listId").asInt();
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.DEVELOPER);

        mvc.perform(post("/data/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("failure to create task (list does not exist)")
    public void createTask_fail_listDoesNotExist() throws Exception{
        final Integer listId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfListDoesNotExist(listId);
        ObjectNode request = buildValidTaskRequest(mapper).put("listId", listId);

        mvc.perform(post("/data/task")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.LIST_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("failure to create a task (assigned user does not exist)")
    public void createTask_fail_assignedUserDoesNotExist() throws Exception{
        final Integer listId = 1;
        final Integer assignedUserId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.DEVELOPER);
        checkIfUserDoesNotExist(assignedUserId);
        ObjectNode request = buildValidTaskRequest(mapper)
                .put("listId", listId)
                .put("assignedUserId", assignedUserId);

        ResultActions resultActions = mvc.perform(post("/data/task/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.USER_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(2)
    @DisplayName("failure to create a task (assigned user has not permissions)")
    public void createTask_fail_assignedUserHasNotPermissions(String assignedUserEmail) throws Exception{
        final Integer listId = 1;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.DEVELOPER);
        User assignedUser = getUserByEmail(assignedUserEmail);
        checkIfUserRoleLower(assignedUser, taskList.getProject(), UserRole.DEVELOPER);
        ObjectNode request = buildValidTaskRequest(mapper)
                .put("listId", listId)
                .put("assignedUserId", assignedUser.getId());

        mvc.perform(post("/data/task/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.ASSIGNING_NOT_GRANTED_USER.name()) ))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    public static Stream<Arguments> updateTask_success(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        "admin@gmail.com",
                        buildValidTaskRequest(mapper)
                                .put("name", "renamed_task")
                                .put("assignedUserId", 2),
                        4
                ),
                Arguments.of(
                        "dev@gmail.com",
                        buildValidTaskRequest(mapper).put("name", "renamed_task"),
                        5
                )
        );
    }

    @ParameterizedTest(name = "#{index} task id: {2}, user: {0}")
    @MethodSource
    @Order(3)
    @DisplayName("successfully update a task")
    public void updateTask_success(String userEmail, ObjectNode request, Integer taskId) throws Exception{
        Task requestTask = buildTaskFromNode(request);
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.DEVELOPER);
        requestTask.setList(task.getList());
        requestTask.setCreator(task.getCreator());

        if(!request.get("assignedUserId").isNull()) {
            User assignedUser = getUserById(request.get("assignedUserId").asInt());
            requestTask.setAssignedUser(assignedUser);
        }

        ResultActions resultActions = mvc.perform(put("/data/task/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.task.id", is(taskId) ))
                .andExpect(jsonPath("$.task.messages", emptyIterable()));
        testTaskDto(resultActions, requestTask);

        task = taskService.findById(taskId).orElse(null);

        Assertions.assertNotNull(task);
        Assertions.assertEquals(requestTask.getName(), task.getName());
        Assertions.assertEquals(requestTask.getAssignedUser(), task.getAssignedUser());
    }

    @Test
    @Order(3)
    @DisplayName("failure to update task (task does not exist)")
    public void updateTask_fail_taskNotFound() throws Exception{
        final Integer taskId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTaskDoesNotExist(taskId);
        ObjectNode request = buildValidTaskRequest(mapper);

        mvc.perform(put("/data/task/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.TASK_NOT_FOUND.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(3)
    @DisplayName("failure to update task (permissions are not granted)")
    public void updateTask_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer taskId = 4;
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleLower(user, task.getList().getProject(), UserRole.DEVELOPER);
        ObjectNode request = buildValidTaskRequest(mapper).put("name", "re_renamed_task");

        mvc.perform(put("/data/task/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource("invalidData")
    @Order(3)
    @DisplayName("failure to update a task (invalid data)")
    public void updateTask_fail_invalidData(ObjectNode request, Error error)  throws Exception{
        final Integer taskId = 4;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.DEVELOPER);

        mvc.perform(put("/data/task/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @Test
    @Order(3)
    @DisplayName("failure to update a task (list does not exist)")
    public void updateTask_fail_listDoesNotExist()  throws Exception{
        final Integer taskId = 4;
        final Integer newListId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfListDoesNotExist(newListId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.DEVELOPER);
        ObjectNode request = buildValidTaskRequest(mapper).put("listId", newListId);

        mvc.perform(put("/data/task/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString())
                .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.LIST_NOT_FOUND.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @Test
    @Order(3)
    @DisplayName("failure to update a task (assigned user does not exist)")
    public void updateTask_fail_assignedUserDoesNotExist()  throws Exception{
        final Integer taskId = 4;
        final Integer newAssignedUserId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.DEVELOPER);
        checkIfUserDoesNotExist(newAssignedUserId);
        ObjectNode request = buildValidTaskRequest(mapper).put("assignedUserId", newAssignedUserId);

        mvc.perform(put("/data/task/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.USER_NOT_FOUND.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(3)
    @DisplayName("failure to update a task (assigned user not granted)")
    public void updateTask_fail_assignedUserNotGranted(String assignedUserEmail)  throws Exception{
        final Integer taskId = 4;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        User assignedUser = getUserByEmail(assignedUserEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.DEVELOPER);
        checkIfUserRoleLower(assignedUser, task.getList().getProject(), UserRole.DEVELOPER);
        ObjectNode request = buildValidTaskRequest(mapper).put("assignedUserId", assignedUser.getId());

        mvc.perform(put("/data/task/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.ASSIGNING_NOT_GRANTED_USER.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(4)
    @DisplayName("failure to delete a task (permissions are not granted)")
    public void deleteTask_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer taskId = 4;
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleLower(user, task.getList().getProject(), UserRole.DEVELOPER);

        mvc.perform(delete("/data/task/" + taskId)
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("$.task", nullValue()));

        task = taskService.findById(taskId).orElse(null);

        Assertions.assertNotNull(task);
    }

    @Test
    @Order(4)
    @DisplayName("failure to delete a task (task does not exist)")
    public void deleteTask_fail_taskDoesNotExist() throws Exception{
        final Integer taskId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTaskDoesNotExist(taskId);

        mvc.perform(delete("/data/task/" + taskId)
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.TASK_NOT_FOUND.name())))
                .andExpect(jsonPath("$.task", nullValue()));
    }

    public static Stream<Arguments> deleteTask_success(){
        return Stream.of(
                Arguments.of(4, "admin@gmail.com"),
                Arguments.of(5, "dev@gmail.com")
        );
    }

    @ParameterizedTest(name = "#{index} task id:{0}, user: {1}")
    @MethodSource
    @Order(5)
    @DisplayName("successfully to delete a task")
    public void deleteTask_success(Integer taskId, String userEmail) throws Exception{
        User user = getUserByEmail(userEmail);
        Task task = getTaskById(taskId);
        checkIfUserRoleGreaterOrEquals(user, task.getList().getProject(), UserRole.DEVELOPER);

        mvc.perform(delete("/data/task/" + taskId)
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.task.id", is(taskId)));

        task = taskService.findById(taskId).orElse(null);
        Assertions.assertNull(task);
    }
}
