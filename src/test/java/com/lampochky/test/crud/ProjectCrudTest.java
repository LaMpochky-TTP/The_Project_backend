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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;
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
public class ProjectCrudTest extends AbstractCrudTest {
    @Autowired
    public ProjectCrudTest(WebApplicationContext context,
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
            @Autowired UserProjectService userProjectService) {
        List<User> users = Arrays.asList(
                new User("user_1", "Test1", "email_1@gmail.com"),
                new User("user_2", "Test1", "email_2@gmail.com"),
                new User("user_3", "Test1", "email_3@gmail.com"),
                new User("user_4", "Test1", "email_4@gmail.com")
        ).stream().map(user -> userService.save(user)).collect(Collectors.toList());
        List<Project> projects = Arrays.asList(
                new Project(null, "project_1"),
                new Project(null, "project_2")
        ).stream().map(project -> {
            project = projectService.save(project);
            UserProject up = new UserProject(null, UserRole.ADMIN, true, users.get(0), project);
            userProjectService.save(up);
            return project;
        }).collect(Collectors.toList());
    }

    @Test
    @Order(1)
    @DisplayName("successfully get the project by id success")
    public void getProject_success() throws Exception {
        final Integer id = 1;
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(id);
        UserRole role = checkIfUserRoleGreaterOrEquals(user, project, UserRole.GUEST);

        mvc.perform(get("/data/project/" + id)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.role", is(role.toString()) ))
                .andExpect(jsonPath("$.project.id", is(id)))
                .andExpect(jsonPath("$.project.name", is(project.getName())))
                .andExpect(jsonPath("$.project.lists[*].id",
                        containsInAnyOrder(project.getLists().stream().map(TaskList::getId).toArray()) ))
                .andExpect(jsonPath("$.project.users[*].id",
                        containsInAnyOrder(project.getUsers().stream().map(up -> up.getUser().getId()).toArray()) ));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get the project by id (user is not participant)")
    public void getProject_fail_notParticipant() throws Exception{
        final Integer id = 1;
        final String userEmail = "email_2@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(id);
        checkIfUserRoleLower(user, project, UserRole.GUEST);

        mvc.perform(get("/data/project/" + id)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", hasItem(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("$.errors", iterableWithSize(1)))
                .andExpect(jsonPath("$.role", nullValue()))
                .andExpect(jsonPath("$.project", nullValue()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get the project by id (project does not exist)")
    public void getProject_fail_projectDoesNotExists() throws Exception {
        final Integer id = 10;
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfProjectDoesNotExist(id);

        mvc.perform(get("/data/project/" + id)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PROJECT_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.role", nullValue()))
                .andExpect(jsonPath("$.project", nullValue()));
    }

    @Test
    @Order(1)
    @DisplayName("successfully get all projects to user")
    public void getAllProjects_success() throws Exception {
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        List<Project> projects = projectService.findAllByUser(user);

        Assumptions.assumeTrue(user != null, "no user found in the db");

        mvc.perform(get("/data/project/all")
                    .with(user(new UserSecurity(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.projects[*].id",
                        containsInAnyOrder(projects.stream().map(Project::getId).toArray()) ))
                .andExpect(jsonPath("$.projects[*].name",
                        containsInAnyOrder(projects.stream().map(Project::getName).toArray()) ))
                .andExpect(jsonPath("$.projects[*].users", everyItem(emptyIterable()) ))
                .andExpect(jsonPath("$.projects[*].lists", everyItem(emptyIterable()) ));
    }

    @Test
    @Order(2)
    @WithUserDetails("email_2@gmail.com")
    @DisplayName("successfully create project")
    public void createProject_success() throws Exception {
        final String userEmail = "email_2@gmail.com";
        final String name = "new_project";
        User user = getUserByEmail(userEmail);
        ObjectNode request = mapper.createObjectNode()
                .put("name", name);

        String response = mvc.perform(post("/data/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.project.id", notNullValue()))
                .andExpect( jsonPath("$.project.name", is(name) ))
                .andReturn().getResponse().getContentAsString();

        Integer projectId = mapper.readTree(response).at("/project/id").asInt();
        Project project = projectService.findById(projectId).orElse(null);

        Assertions.assertNotNull(project);
        Assertions.assertEquals(name, project.getName());
        Assertions.assertIterableEquals(Collections.singletonList(user),
                project.getUsers().stream().map(UserProject::getUser).collect(Collectors.toList()));
        Assertions.assertEquals(UserRole.ADMIN, project.getUsers().get(0).getRole());
    }

    public static Stream<Arguments> projectInvalidData(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", ""),
                        Error.NAME_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("name", "too_long_name_of_new_project"),
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
    @MethodSource("projectInvalidData")
    @Order(2)
    @DisplayName("failure to create project (invalid data)")
    public void createProject_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final String userEmail = "email_2@gmail.com";
        User user = getUserByEmail(userEmail);

        mvc.perform(post("/data/project")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Stream.of(error).map(Error::name).toArray())))
                .andExpect(jsonPath("$.project", nullValue()));
    }

    public static Stream<Arguments> inviteToProjectByIdentifier_success() {
        return Stream.of(
                Arguments.of("email_2@gmail.com", UserRole.DEVELOPER),
                Arguments.of("user_3", UserRole.GUEST)
        );
    }

    @ParameterizedTest(name = "#{index} {0} as {1}")
    @MethodSource
    @Order(2)
    @DisplayName("successful inviting user to the project by username or email")
    public void inviteToProjectByIdentifier_success(String userIdentifier, UserRole role) throws Exception{
        final Integer projectId = 1;
        final String invitorEmail = "email_1@gmail.com";
        User invitor = getUserByEmail(invitorEmail);
        Project project = getProjectById(projectId);
        User user = getUserByIdentifier(userIdentifier);
        checkIfUserRoleGreaterOrEquals(invitor, project, UserRole.ADMIN);
        ObjectNode request = mapper.createObjectNode()
                .put("userIdentifier", userIdentifier)
                .put("role", role.name());

        UserProject userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);
        Assumptions.assumeTrue(userRelation == null, "user is already invited");

        mvc.perform(post("/data/project/" + projectId + "/invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(invitor)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.userIdentifier", is(userIdentifier)))
                .andExpect(jsonPath("$.projectId", is(projectId)));

        userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);

        Assertions.assertNotNull(userRelation);
        Assertions.assertFalse(userRelation.getConfirmed());
        Assertions.assertEquals(role, userRelation.getRole());
    }

    public static Stream<String> inviteToProjectByIdentifier_fail_userAlreadyInvited() {
        return Stream.of("email_2@gmail.com", "email_1@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource
    @Order(3)
    @DisplayName("failure to invite user ot the project (user is already invited)")
    public void inviteToProjectByIdentifier_fail_userAlreadyInvited(String userEmail) throws Exception {
        final Integer projectId = 1;
        final String invitorEmail = "email_1@gmail.com";
        final UserRole role = UserRole.DEVELOPER;
        User invitor = getUserByEmail(invitorEmail);
        Project project = getProjectById(projectId);
        User user = getUserByEmail(userEmail);
        checkIfUserRoleGreaterOrEquals(invitor, project, UserRole.ADMIN);
        ObjectNode request = mapper.createObjectNode()
                .put("userIdentifier", userEmail)
                .put("role", role.name());

        UserProject userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);
        Assumptions.assumeTrue(userRelation != null, "user is not invited yet");

        mvc.perform(post("/data/project/" + projectId + "/invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(invitor)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.USER_IS_ALREADY_MEMBER.name())))
                .andExpect(jsonPath("$.userIdentifier", is(userEmail)))
                .andExpect(jsonPath("$.projectId", is(projectId)));
    }

    @Test
    @Order(4)
    @DisplayName("successfully confirming inviting to the project")
    public void confirmInvitingToProject_success() throws Exception {
        final Integer projectId = 1;
        final String userEmail = "email_2@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        ObjectNode request = mapper.createObjectNode()
                .put("confirm", true);

        UserProject userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);
        Assumptions.assumeTrue(userRelation != null && !userRelation.getConfirmed(),
                "no unconfirmed relation found in the db");

        mvc.perform(put("/data/project/" + projectId + "/invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success", is(true)))
                .andExpect(jsonPath("errors", emptyIterable()))
                .andExpect(jsonPath("projectId", is(projectId)));

        userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);

        Assertions.assertNotNull(userRelation);
        Assertions.assertTrue(userRelation.getConfirmed());
    }

    @Test
    @Order(4)
    @DisplayName("successfully refuting inviting to the project")
    public void refuteInvitingToProject_success() throws Exception {
        final Integer projectId = 1;
        final String userEmail = "email_3@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        ObjectNode request = mapper.createObjectNode()
                .put("confirm", false);

        UserProject userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);
        Assumptions.assumeTrue(userRelation != null && !userRelation.getConfirmed(),
                "no unconfirmed relation found in the db");

        mvc.perform(put("/data/project/" + projectId + "/invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success", is(true)))
                .andExpect(jsonPath("errors", emptyIterable()))
                .andExpect(jsonPath("projectId", is(projectId)));

        userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);

        Assertions.assertNull(userRelation);
    }

    @Test
    @Order(4)
    @DisplayName("failure to confirm inviting to the project (user not invited)")
    public void confirmInvitingToProject_fail_noInvition() throws Exception{
        final Integer projectId = 1;
        final String userEmail = "email_4@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        ObjectNode request = mapper.createObjectNode()
                .put("confirm", true);

        UserProject userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);
        Assumptions.assumeTrue(userRelation == null, "relation found in the db");

        mvc.perform(put("/data/project/" + projectId + "/invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName", containsInAnyOrder(Error.INVITION_NOT_FOUND.name())))
                .andExpect(jsonPath("projectId", is(projectId)));
    }

    @Test
    @Order(5)
    @DisplayName("failure to refute inviting to the project (inviting already confirmed) ")
    public void refuteInvitingToProject_failure_alreadyConfirmed() throws Exception{
        final Integer projectId = 1;
        final String userEmail = "email_2@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        ObjectNode request = mapper.createObjectNode()
                .put("confirm", false);

        UserProject userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);
        Assumptions.assumeTrue(userRelation != null && userRelation.getConfirmed(),
                "no confirmed relation found in the db");

        mvc.perform(put("/data/project/" + projectId + "/invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName",
                        containsInAnyOrder(Error.USER_IS_ALREADY_MEMBER.name())))
                .andExpect(jsonPath("projectId", is(projectId)));

        userRelation = userProjectService.findByUserAndProject(user, project).orElse(null);

        Assertions.assertNotNull(userRelation);
    }

    @Test
    @Order(5)
    @DisplayName("successfully updating the project")
    public void updateProject_success() throws Exception{
        final Integer projectId = 1;
        final String userEmail = "email_1@gmail.com";
        final String newName = "new_name";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.ADMIN);
        ObjectNode request = mapper.createObjectNode()
                .put("name", newName);

        mvc.perform(put("/data/project/" + projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success", is(true)))
                .andExpect(jsonPath("errors", emptyIterable()))
                .andExpect(jsonPath("project.id", is(projectId)))
                .andExpect(jsonPath("project.name", is(newName)))
                .andExpect(jsonPath("project.users", emptyIterable()))
                .andExpect(jsonPath("project.lists", emptyIterable()));

        project = projectService.findById(projectId).orElse(null);

        Assertions.assertNotNull(project);
        Assertions.assertEquals(newName, project.getName());
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource("projectInvalidData")
    @Order(5)
    @DisplayName("failure to update the project (invalid data)")
    public void updateProject_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer projectId = 1;
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.ADMIN);

        mvc.perform(put("/data/project/" + projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName", containsInAnyOrder(error.name())))
                .andExpect(jsonPath("project", nullValue()));
    }

    @Test
    @Order(5)
    @DisplayName("failure to update the project (project does not exist)")
    public void updateProject_fail_projectDoesNotExists() throws Exception{
        final Integer projectId = 10;
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfProjectDoesNotExist(projectId);
        ObjectNode request = mapper.createObjectNode()
                .put("name", "new_name");

        mvc.perform(put("/data/project/" + projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName", containsInAnyOrder(Error.PROJECT_NOT_FOUND.name())))
                .andExpect(jsonPath("project", nullValue()));
    }

    public static Stream<String> notAdmins() {
        return Stream.of("email_2@gmail.com", "email_3@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notAdmins")
    @Order(5)
    @DisplayName("failure to update the project (permissions not granted)")
    public void updateProject_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer projectId = 1;
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleLower(user, project, UserRole.ADMIN);
        ObjectNode request = mapper.createObjectNode()
                .put("name", "new_name");

        mvc.perform(put("/data/project/" + projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("project", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notAdmins")
    @Order(6)
    @DisplayName("failure to delete the project (permissions not granted)")
    public void deleteProject_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer projectId = 1;
        User user = getUserByEmail(userEmail);
        Project project = projectService.findById(projectId).orElse(null);
        checkIfUserRoleLower(user, project, UserRole.ADMIN);

        mvc.perform(delete("/data/project/" + projectId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name())))
                .andExpect(jsonPath("project", nullValue()));

        project = projectService.findById(projectId).orElse(null);

        Assertions.assertNotNull(project);
    }

    @Test
    @Order(6)
    @DisplayName("failure to delete the project (project does not exist)")
    public void deleteProject_fail_projectDoesNotExists() throws Exception{
        final Integer projectId = 10;
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfProjectDoesNotExist(projectId);

        mvc.perform(delete("/data/project/" + projectId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("success", is(false)))
                .andExpect(jsonPath("errors[*].errName", containsInAnyOrder(Error.PROJECT_NOT_FOUND.name())))
                .andExpect(jsonPath("project", nullValue()));
    }

    @Test
    @Order(7)
    @DisplayName("successfully delete the project")
    public void deleteProject_success() throws Exception{
        final Integer projectId = 1;
        final String userEmail = "email_1@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.ADMIN);

        mvc.perform(delete("/data/project/" + projectId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("success", is(true)))
                .andExpect(jsonPath("errors", emptyIterable()))
                .andExpect(jsonPath("project.id", is(projectId)))
                .andExpect(jsonPath("project.name", is(project.getName()) ));

        project = projectService.findById(projectId).orElse(null);

        Assertions.assertNull(project);
    }
}
