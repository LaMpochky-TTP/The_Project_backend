package com.lampochky.test.crud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lampochky.database.entity.*;
import com.lampochky.database.service.*;
import com.lampochky.dto.DtoConstants;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public abstract class AbstractCrudTest {
    private final WebApplicationContext context;

    protected final UserService userService;
    protected final ProjectService projectService;
    protected final UserProjectService userProjectService;
    protected final ListService listService;
    protected final TaskService taskService;
    protected final TagService tagService;
    protected final MessageService messageService;

    protected final ObjectMapper mapper;

    protected static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DtoConstants.dateFormat);
    protected static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(DtoConstants.timeFormat);
    protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DtoConstants.dateTimeFormat);

    protected MockMvc mvc;

    public AbstractCrudTest(WebApplicationContext context,
                            UserService userService,
                            ProjectService projectService,
                            UserProjectService userProjectService,
                            ListService listService,
                            TaskService taskService,
                            TagService tagService,
                            MessageService messageService) {
        this.context = context;
        this.userService = userService;
        this.projectService = projectService;
        this.userProjectService = userProjectService;
        this.listService = listService;
        this.taskService = taskService;
        this.tagService = tagService;
        this.messageService = messageService;
        mapper = new ObjectMapper();
    }

    @BeforeEach
    public void createMockMvc(){
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(MockMvcResultHandlers.print())
                .build();
    }

    protected boolean roleGreaterOrEquals(UserProject relation, UserRole role){
        return relation != null && relation.getConfirmed() && relation.getRole().greaterOrEquals(role);
    }

    protected User getUserById(Integer id){
        Optional<User> optUser = userService.findById(id);
        Assumptions.assumeTrue(optUser.isPresent(), "user is not found in the db");
        return optUser.get();
    }

    protected User getUserByEmail(String userEmail) {
        Optional<User> optUser = userService.findByEmail(userEmail);
        Assumptions.assumeTrue(optUser.isPresent(), "user is not found in the db");
        return optUser.get();
    }

    protected User getUserByIdentifier(String identifier) {
        Optional<User> optUser = userService.findByUsernameOrEmail(identifier, identifier);
        Assumptions.assumeTrue(optUser.isPresent(), "no user found in the db");
        return optUser.get();
    }

    protected void checkIfUserDoesNotExist(Integer id) {
        Optional<User> optUser = userService.findById(id);
        Assumptions.assumeFalse(optUser.isPresent(), "user is found in the db");
    }

    protected UserRole checkIfUserRoleGreaterOrEquals(User user, Project project, UserRole role) {
        Optional<UserProject> optRelation = userProjectService.findByUserAndProject(user, project);
        Assumptions.assumeTrue(optRelation.isPresent() && optRelation.get().getConfirmed() &&
                optRelation.get().getRole().greaterOrEquals(role), "permissions are not found in the db");
        return optRelation.get().getRole();
    }

    protected void checkIfUserRoleLower(User user, Project project, UserRole role) {
        Optional<UserProject> optRelation = userProjectService.findByUserAndProject(user, project);
        Assumptions.assumeTrue(!optRelation.isPresent() || !optRelation.get().getConfirmed() ||
                !optRelation.get().getRole().greaterOrEquals(role), "permissions are found in the db");
    }

    protected Project getProjectById(Integer id) {
        Optional<Project> optProject = projectService.findById(id);
        Assumptions.assumeTrue(optProject.isPresent(), "project is not found in the db");
        return optProject.get();
    }

    protected void checkIfProjectDoesNotExist(Integer id) {
        Optional<Project> optProject = projectService.findById(id);
        Assumptions.assumeFalse(optProject.isPresent(), "project is found in the db");
    }

    protected TaskList getListById(Integer id) {
        Optional<TaskList> optList = listService.findById(id);
        Assumptions.assumeTrue(optList.isPresent(), "list is not found in the db");
        return optList.get();
    }

    protected void checkIfListDoesNotExist(Integer id) {
        Optional<TaskList> optList = listService.findById(id);
        Assumptions.assumeFalse(optList.isPresent(), "list is found in the db");
    }

    protected Task getTaskById(Integer id) {
        Optional<Task> optTask = taskService.findById(id);
        Assumptions.assumeTrue(optTask.isPresent(), "task is not found in the db");
        return optTask.get();
    }

    protected void checkIfTaskDoesNotExist(Integer id) {
        Optional<Task> optTask = taskService.findById(id);
        Assumptions.assumeFalse(optTask.isPresent(), "task is found in the db");
    }

    protected Tag getTagById(Integer id) {
        Optional<Tag> optTag = tagService.findById(id);
        Assumptions.assumeTrue(optTag.isPresent(), "tag is not found in the db");
        return optTag.get();
    }

    protected void checkIfTagDoesNotExist(Integer id) {
        Optional<Tag> optTag = tagService.findById(id);
        Assumptions.assumeFalse(optTag.isPresent(), "tag is found in the db");
    }

    protected Message getMessageById(Integer id) {
        Optional<Message> optMessage = messageService.findById(id);
        Assumptions.assumeTrue(optMessage.isPresent(), "message is not found in the db");
        return optMessage.get();
    }

    protected void checkIfMessageDoesNotExist(Integer id) {
        Optional<Message> optMessage = messageService.findById(id);
        Assumptions.assumeFalse(optMessage.isPresent(), "message is found in the db");
    }



}
