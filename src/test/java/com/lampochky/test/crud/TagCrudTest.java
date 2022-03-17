package com.lampochky.test.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lampochky.Application;
import com.lampochky.database.entity.Tag;
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
public class TagCrudTest extends AbstractCrudTest {
    @Autowired
    public TagCrudTest(WebApplicationContext context,
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
            @Autowired TagService tagService){
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
                new Tag("tag_1", project),
                new Tag("tag_2", project)
        ).forEach(tagService::save);
    }

    public static Stream<String> participants(){
        return Stream.of("admin@gmail.com", "dev@gmail.com", "guest@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participants")
    @Order(1)
    @DisplayName("successfully get task by id")
    public void getTag_success(String userEmail) throws Exception{
        final Integer tagId = 1;
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleGreaterOrEquals(user, tag.getProject(), UserRole.GUEST);

        mvc.perform(get("/data/tag/" + tagId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.id", is(tagId)))
                .andExpect(jsonPath("$.tag.id", is(tag.getId()) ))
                .andExpect(jsonPath("$.tag.projectId", is(tag.getProject().getId()) ))
                .andExpect(jsonPath("$.tag.name", is(tag.getName()) ));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get task by id (task does not exist)")
    public void getTag_fail_tagDoesNotExist() throws Exception{
        final Integer tagId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTagDoesNotExist(tagId);

        mvc.perform(get("/data/tag/" + tagId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.TAG_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.id", is(tagId)))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get task by id (permissions are not granted)")
    public void getTag_fail_permissionsNotGranted() throws Exception{
        final Integer tagId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleLower(user, tag.getProject(), UserRole.GUEST);

        mvc.perform(get("/data/tag/" + tagId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.id", is(tagId)))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("participants")
    @Order(1)
    @DisplayName("successfully get all tasks in a project")
    public void getTagsInProject_success(String userEmail) throws Exception{
        final Integer projectId = 1;
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        List<Tag> tags = project.getTags();
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.GUEST);

        mvc.perform(get("/data/tag/in_project")
                    .param("id", projectId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.projectId", is(projectId)))
                .andExpect(jsonPath("$.tags[*].id",
                        containsInAnyOrder(tags.stream().map(Tag::getId).toArray()) ))
                .andExpect(jsonPath("$.tags[*].name",
                        containsInAnyOrder(tags.stream().map(Tag::getName).toArray()) ))
                .andExpect(jsonPath("$.tags[*].projectId", everyItem(is(projectId)) ));

    }

    @Test
    @Order(1)
    @DisplayName("failure to get all tasks in a project (project does not exist)")
    public void getTagsInProject_fail_projectDoesNotExist() throws Exception{
        final Integer projectId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfProjectDoesNotExist(projectId);

        mvc.perform(get("/data/tag/in_project")
                    .param("id", projectId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PROJECT_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.projectId", is(projectId)))
                .andExpect(jsonPath("$.tags", emptyIterable()));
    }

    @Test
    @Order(1)
    @DisplayName("failure to get all tasks in a project (permissions are not granted)")
    public void getTagsInProject_fail_permissionsNotGranted() throws Exception{
        final Integer projectId = 1;
        final String userEmail = "nor@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleLower(user, project, UserRole.GUEST);

        mvc.perform(get("/data/tag/in_project")
                    .param("id", projectId.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.projectId", is(projectId)))
                .andExpect(jsonPath("$.tags", emptyIterable()));
    }

    public static Stream<String> devs() {
        return Stream.of("admin@gmail.com", "dev@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("devs")
    @Order(2)
    @DisplayName("successfully create a tag")
    public void createTag_success(String userEmail) throws Exception{
        final Integer projectId = 1;
        final String name = "new_tag";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.DEVELOPER);
        ObjectNode request = mapper.createObjectNode()
                .put("projectId", projectId)
                .put("name", "new_tag");

        String response = mvc.perform(post("/data/tag/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.tag.id", notNullValue()))
                .andExpect(jsonPath("$.tag.name", is(name)))
                .andExpect(jsonPath("$.tag.projectId", is(projectId)))
                .andReturn().getResponse().getContentAsString();

        Tag tag = tagService.findById(mapper.readTree(response).at("/tag/id").asInt()).orElse(null);

        Assertions.assertNotNull(tag);
        Assertions.assertEquals(name, tag.getName());
        Assertions.assertEquals(projectId, tag.getProject().getId());
    }

    public static Stream<String> notDevs() {
        return Stream.of("guest@gmail.com", "nor@gmail.com");
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(2)
    @DisplayName("failure to create a tag (permissions are not granted)")
    public void createTag_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer projectId = 1;
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleLower(user, project, UserRole.DEVELOPER);
        ObjectNode request = mapper.createObjectNode()
                .put("projectId", projectId)
                .put("name", "new_tag");

        mvc.perform(post("/data/tag/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    @Test
    @Order(2)
    @DisplayName("failure to create a tag (project does not exist)")
    public void createTag_fail_projectDoesNotExists() throws Exception{
        final Integer projectId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfProjectDoesNotExist(projectId);
        ObjectNode request = mapper.createObjectNode()
                .put("projectId", projectId)
                .put("name", "new_tag");

        mvc.perform(post("/data/tag/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PROJECT_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    public static Stream<Arguments> createTag_fail_invalidData(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                    mapper.createObjectNode()
                        .put("projectId", 1)
                        .put("name", ""),
                    Error.NAME_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("projectId", 1)
                                .put("name", "too_long_name_of_a_tag"),
                        Error.NAME_TOO_LONG
                ),
                Arguments.of(
                        mapper.createObjectNode()
                                .put("projectId", 1)
                                .put("name", "illegal char$"),
                        Error.NAME_ILLEGAL_CHARACTERS
                )
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource
    @Order(2)
    @DisplayName("failure to create a tag (invalid data)")
    public void createTag_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer projectId = request.get("projectId").asInt();
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Project project = getProjectById(projectId);
        checkIfUserRoleGreaterOrEquals(user, project, UserRole.DEVELOPER);

        mvc.perform(post("/data/tag/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    public static Stream<Arguments> updateTag_success() {
        return Stream.of(
                Arguments.of(3, "admin@gmail.com"),
                Arguments.of(4, "dev@gmail.com")
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource
    @Order(3)
    @DisplayName("successfully update a tag")
    public void updateTag_success(Integer tagId, String userEmail) throws Exception{
        final String newName = "renamed_" + tagId;
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleGreaterOrEquals(user, tag.getProject(), UserRole.DEVELOPER);
        ObjectNode request = mapper.createObjectNode()
                .put("name", newName);

        mvc.perform(put("/data/tag/" + tagId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.tag.id", is(tagId)))
                .andExpect(jsonPath("$.tag.name", is(newName)));

        tag = tagService.findById(tagId).orElse(null);

        Assertions.assertNotNull(tag);
        Assertions.assertEquals(newName, tag.getName());
    }

    @Test
    @Order(3)
    @DisplayName("failure to update a tag (tag does not exist)")
    public void updateTag_fail_tagDoesNotExist() throws Exception{
        final Integer tagId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTagDoesNotExist(tagId);
        ObjectNode request = mapper.createObjectNode()
                .put("name", "new_name");

        mvc.perform(put("/data/tag/" + tagId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.TAG_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(3)
    @DisplayName("failure to update a tag (permissions are not granted)")
    public void updateTag_fail_permissionsNotGranted(String userEmail) throws Exception{
        final Integer tagId = 3;
        final String newName = "renamed_" + tagId;
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleLower(user, tag.getProject(), UserRole.DEVELOPER);
        ObjectNode request = mapper.createObjectNode()
                .put("name", newName);

        mvc.perform(put("/data/tag/" + tagId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    public static Stream<Arguments> updateTag_fail_invalidData(){
        ObjectMapper mapper = new ObjectMapper();
        return Stream.of(
                Arguments.of(
                        mapper.createObjectNode().put("name", ""),
                        Error.NAME_EMPTY
                ),
                Arguments.of(
                        mapper.createObjectNode().put("name", "too_long_name_of_a_tag"),
                        Error.NAME_TOO_LONG
                ),
                Arguments.of(
                        mapper.createObjectNode().put("name", "illegal char$"),
                        Error.NAME_ILLEGAL_CHARACTERS
                )
        );
    }

    @ParameterizedTest(name = "#{index} {1}")
    @MethodSource
    @Order(3)
    @DisplayName("failure to update a tag (invalid data)")
    public void updateTag_fail_invalidData(ObjectNode request, Error error) throws Exception{
        final Integer tagId = 3;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleGreaterOrEquals(user, tag.getProject(), UserRole.DEVELOPER);

        mvc.perform(put("/data/tag/" + tagId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request.toString())
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(error.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    @Test
    @Order(4)
    @DisplayName("failure to delete a tag (tag does not exist)")
    public void deleteTag_fail_tagDoesNotExist() throws Exception{
        final Integer tagId = 10;
        final String userEmail = "admin@gmail.com";
        User user = getUserByEmail(userEmail);
        checkIfTagDoesNotExist(tagId);

        mvc.perform(delete("/data/tag/" + tagId)
                    .with( user(new UserSecurity(user)) ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName", containsInAnyOrder(Error.TAG_NOT_FOUND.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    @ParameterizedTest(name = "#{index} {0}")
    @MethodSource("notDevs")
    @Order(4)
    @DisplayName("failure to delete a tag (permissions are not granted)")
    public void deleteTag_fail_permissionNotGranted(String userEmail) throws Exception{
        final Integer tagId = 3;
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleLower(user, tag.getProject(), UserRole.DEVELOPER);

        mvc.perform(delete("/data/tag/" + tagId)
                    .with( user(new UserSecurity(user)) ))
                    .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors[*].errName",
                        containsInAnyOrder(Error.PERMISSIONS_NOT_GRANTED.name()) ))
                .andExpect(jsonPath("$.tag", nullValue()));
    }

    public static Stream<Arguments> deleteTag_success(){
        return Stream.of(
                Arguments.of(3, "admin@gmail.com"),
                Arguments.of(4, "dev@gmail.com")
        );
    }

    @ParameterizedTest(name = "#{index} tag''s id: {0}, user: {1}")
    @MethodSource
    @Order(5)
    @DisplayName("successfully delete a tag")
    public void deleteTag_success(Integer tagId, String userEmail) throws Exception{
        User user = getUserByEmail(userEmail);
        Tag tag = getTagById(tagId);
        checkIfUserRoleGreaterOrEquals(user, tag.getProject(), UserRole.DEVELOPER);

        mvc.perform(delete("/data/tag/" + tagId)
                    .with( user(new UserSecurity(user)) ))
                    .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.errors", emptyIterable()))
                .andExpect(jsonPath("$.tag.id", is(tagId)))
                .andExpect(jsonPath("$.tag.name", is(tag.getName())))
                .andExpect(jsonPath("$.tag.projectId", is(tag.getProject().getId())));

        tag = tagService.findById(tagId).orElse(null);

        Assertions.assertNull(tag);
    }
}
