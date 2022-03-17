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

import java.util.Arrays;
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
public class ListCrudTest extends AbstractCrudTest {
    @Autowired
    public ListCrudTest(WebApplicationContext context,
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
    public static void beforeAll(
            @Autowired UserService userService,
            @Autowired ProjectService projectService,
            @Autowired UserProjectService userProjectService,
            @Autowired ListService listService){
        Project project = projectService.save(new Project(null, "project"));
        List<User> users = Arrays.asList(
                new User("admin", "Test1", "admin@gmail.com"),
                new User("dev", "Test1", "dev@gmail.com"),
                new User("guest", "Test1", "guest@gmail.com"),
                new User("nor", "Test1", "nor@gmail.com")
        ).stream().map(u -> userService.save(u)).collect(Collectors.toList());
        for(int i = 0; i < users.size() - 1; ++i){
            UserProject up = new UserProject(null, UserRole.values()[i], true, users.get(i), project);
            userProjectService.save(up);
        }
        Stream.of(
                new TaskList(null, "list_1", project),
                new TaskList(null, "list_2", project)
        ).forEach(listService::save);
    }

    public static Stream<String> participantEmails(){
        return Stream.of("admin@gmail.com", "dev@gmail.com", "guest@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participantEmails")
    @Order(1)
    @DisplayName("successfully get a list")
    public void getList_success(String userEmail) throws Exception{
        final Integer listId = 1;
        TaskList list = getListById(listId);
        User user = getUserByEmail(userEmail);
        checkIfUserRoleGreaterOrEquals(user, list.getProject(), UserRole.GUEST);

        mvc.perform(get("/data/list/" + listId)
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.list.id", is(listId)))
                .andExpect(jsonPath("$.list.name", is(list.getName())))
                .andExpect(jsonPath("$.list.projectId", is(list.getProject().getId())));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get a list (user is not participant)")
    public void getList_fail_notParticipant() throws Exception{
        final Integer listId = 1;
        final String userEmail = "nor@gmail.com";
        TaskList list = getListById(listId);
        User user = getUserByEmail(userEmail);
        checkIfUserRoleLower(user, list.getProject(), UserRole.GUEST);

        mvc.perform(get("/data/list/" + listId)
                .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get a list (list does not exist)")
    public void getList_fail_listDoesNotExist() throws Exception{
        final Integer listId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfListDoesNotExist(listId);

        mvc.perform(get("/data/list/" + listId)
                .with(user(new UserSecurity(user))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.LIST_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participantEmails")
    @Order(1)
    @DisplayName("successfully get all lists in the project")
    public void getListsInProject_success(String userEmail) throws Exception{
        final Integer projectId = 1;
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        List<TaskList> lists = project.getLists();
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.GUEST);

        mvc.perform(get("/data/list/in_project")
                    .param("id", projectId.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.projectId", is(projectId)))
                .andExpect(jsonPath("$.lists[*].id",
                        containsInAnyOrder(lists.stream().map(TaskList::getId).toArray()) ))
                .andExpect(jsonPath("$.lists[*].name",
                        containsInAnyOrder(lists.stream().map(TaskList::getName).toArray()) ))
                .andExpect(jsonPath("$.lists[*].projectId",
                        containsInAnyOrder(lists.stream().map(l -> l.getProject().getId()).toArray()) ))
                .andExpect(jsonPath("$.lists[*].tasks", everyItem(emptyIterable()) ));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get all lists in the project (user is not participant)")
    public void getListsInProject_fail_notParticipant() throws Exception{
        final Integer projectId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleLower(user, project, UserRole.GUEST);

        mvc.perform(get("/data/list/in_project")
                .param("id", projectId.toString())
                .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.projectId", is(projectId)))
                .andExpect(jsonPath("$.lists", emptyIterable() ));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get all lists in the project (project does not exist)")
    public void getListsInProject_fail_projectDoesNotExist() throws Exception{
        final Integer projectId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfProjectDoesNotExist(projectId);

        mvc.perform(get("/data/list/in_project")
                .param("id", projectId.toString())
                .with(user(new UserSecurity(user))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PROJECT_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.projectId", is(projectId)))
                .andExpect(jsonPath("$.lists", emptyIterable() ));
    }

    @Test
    @Order(2)
    @DisplayName("successfully create a list")
    public void createList_success() throws Exception {
        final Integer projectId = 1;
        final String listName = "new_list";
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.ADMIN);
        ObjectNode node = mapper.createObjectNode()
                .put("name", listName)
                .put("projectId", projectId);

        String response = mvc.perform(post("/data/list")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(node.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.list.id", notNullValue()))
                .andExpect(jsonPath("$.list.name", is(listName)))
                .andExpect(jsonPath("$.list.projectId", is(projectId)))
                .andReturn().getResponse().getContentAsString();

        Integer listId = mapper.readTree(response).at("/list/id").asInt();
        TaskList taskList = listService.findById(listId).orElse(null);

        Assertions.assertNotNull(taskList);
        Assertions.assertEquals(projectId, taskList.getProject().getId());
        Assertions.assertEquals(listName, taskList.getName());
    }

    public static Stream<Arguments> createList_fail_invalidData(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "new_list")
                                .put("projectId", 10),
                        Error.PROJECT_NOT_FOUND
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "")
                                .put("projectId", 1),
                        Error.NAME_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "too_long_name_of_the_list")
                                .put("projectId", 1),
                        Error.NAME_TOO_LONG
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "illegal character$")
                                .put("projectId", 1),
                        Error.NAME_ILLEGAL_CHARACTERS
                )
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource
    @Order(2)
    @DisplayName("failure to create a list (invalid data)")
    public void createList_fail_invalidData(ObjectNode request, Error error) throws Exception {
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);

        mvc.perform(post("/data/list")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));
    }

    public static Stream<String> notAdmins(){
        return Stream.of("dev@gmail.com", "guest@gmail.com", "nor@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notAdmins")
    @Order(2)
    @DisplayName("failure to create a list (permissions are not granted)")
    public void createList_fail_permissionsNotGranted(String userEmail) throws Exception {
        final Integer projectId = 1;
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleLower(user, project, UserRole.ADMIN);
        ObjectNode request = mapper.createObjectNode()
                .put("name", "new_list")
                .put("projectId", projectId);

        mvc.perform(post("/data/list")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));
    }

    public static Stream<String> updateList_success(){
        return Stream.of("dev@gmail.com", "admin@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource
    @Order(3)
    @DisplayName("successfully update a list")
    public void updateList_success(String userEmail) throws Exception {
        final Integer listId = 3;
        final String newName = "renamed_list";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        Project project = taskList.getProject();
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.DEVELOPER);
        ObjectNode request = mapper.createObjectNode()
                .put("name", newName);


        mvc.perform(put("/data/list/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.list.id", is(listId)))
                .andExpect(jsonPath("$.list.name", is(newName)))
                .andExpect(jsonPath("$.list.projectId", is(project.getId())));

        taskList = listService.findById(listId).orElse(null);

        Assertions.assertNotNull(taskList);
        Assertions.assertEquals(newName, taskList.getName());
        Assertions.assertEquals(project.getId(), taskList.getProject().getId());
    }

    public static Stream<String> updateList_fail_permissionsNotGranted(){
        return Stream.of("guest@gmail.com", "nor@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource
    @Order(3)
    @DisplayName("failure to update a list (permissions are not granted)")
    public void updateList_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer listId = 3;
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleLower(user, taskList.getProject(), UserRole.DEVELOPER);
        ObjectNode request = mapper.createObjectNode()
                .put("name", "re_renamed_list");

        mvc.perform(put("/data/list/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));

        TaskList taskList2 = listService.findById(listId).orElse(null);

        Assertions.assertNotNull(taskList2);
        Assertions.assertEquals(taskList.getName(), taskList2.getName());
        Assertions.assertEquals(taskList.getProject().getId(), taskList2.getProject().getId());
    }

    public static Stream<Arguments> updateList_fail_invalidData(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", ""),
                        Error.NAME_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "too_long_name_of_the_list"),
                        Error.NAME_TOO_LONG
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "illegal character$"),
                        Error.NAME_ILLEGAL_CHARACTERS
                )
        );
    }


    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource
    @Order(3)
    @DisplayName("failure to update a list (invalid data)")
    public void updateList_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer listId = 3;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.DEVELOPER);

        mvc.perform(put("/data/list/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));

        TaskList taskList2 = listService.findById(listId).orElse(null);

        Assertions.assertNotNull(taskList2);
        Assertions.assertEquals(taskList.getName(), taskList2.getName());
        Assertions.assertEquals(taskList.getProject().getId(), taskList2.getProject().getId());
    }

    @Test
    @Order(3)
    @DisplayName("failure to update a list (list does not exist)")
    public void updateList_fail_listDoesNotExist() throws Exception{
        final Integer listId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfListDoesNotExist(listId);
        ObjectNode request = mapper.createObjectNode()
                .put("name", "renamed_list");

        mvc.perform(put("/data/list/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.LIST_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notAdmins")
    @Order(4)
    @DisplayName("failure to delete a list (permissions are not granted)")
    public void deleteList_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer listId = 3;
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleLower(user, taskList.getProject(), UserRole.ADMIN);

        mvc.perform(delete("/data/list/" + listId)
                    .with(user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));

        taskList = listService.findById(listId).orElse(null);

        Assertions.assertNotNull(taskList);
    }

    @Test
    @Order(4)
    @DisplayName("failure to delete a list (list does not exist)")
    public void deleteList_fail_listDoesNotExist() throws Exception{
        final Integer listId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfListDoesNotExist(listId);

        mvc.perform(delete("/data/list/" + listId)
                    .with(user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.LIST_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.list", nullValue()));
    }

    @Test
    @Order(5)
    @DisplayName("successfully delete a list")
    public void deleteList_success() throws Exception{
        final Integer listId = 3;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        TaskList taskList = getListById(listId);
        checkIfUserRoleGreaterOrEquals(user, taskList.getProject(), UserRole.ADMIN);

        mvc.perform(delete("/data/list/" + listId)
                    .with(user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.list.id", is(taskList.getId()) ))
                .andExpect(jsonPath("$.list.name", is(taskList.getName()) ))
                .andExpect(jsonPath("$.list.projectId", is(taskList.getProject().getId()) ));

        taskList = listService.findById(listId).orElse(null);

        Assertions.assertNull(taskList);
    }

}
