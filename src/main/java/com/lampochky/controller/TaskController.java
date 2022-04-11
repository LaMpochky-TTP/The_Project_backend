package com.lampochky.controller;

import com.lampochky.database.entity.Task;
import com.lampochky.database.entity.TaskList;
import com.lampochky.database.entity.User;
import com.lampochky.database.entity.UserRole;
import com.lampochky.database.service.*;
import com.lampochky.dto.request.task.TaskRequestDto;
import com.lampochky.dto.response.task.GetTaskByIdResponseDto;
import com.lampochky.dto.response.task.GetTaskByListResponseDto;
import com.lampochky.dto.response.task.TaskResponseDto;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.validation.Error;
import com.lampochky.validation.TaskValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/data/task")
public class TaskController extends AbstractController{
    private final UserService userService;
    private final TaskService taskService;
    private final ListService listService;
    private final ProjectService projectService;
    private final TaskValidator validator;

    @Autowired
    public TaskController(UserProjectService userProjectService, UserService userService,
                          TaskService taskService, ListService listService, ProjectService projectService) {
        super(userProjectService);
        this.userService = userService;
        this.taskService = taskService;
        this.listService = listService;
        this.projectService = projectService;
        validator = new TaskValidator(userService);
    }

    private void setBasicFields(Task task, TaskRequestDto request) {
        task.setName(request.getName());
        task.setDateToStart(request.getDateToStart());
        task.setDateToFinish(request.getDateToFinish());
        task.setPriority(request.getPriority());
        task.setDescription(request.getDescription());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetTaskByIdResponseDto> getById(@AuthenticationPrincipal UserSecurity authUser,
                                                          @PathVariable("id") Integer id) {
        User user = authUser.getUser();
        Optional<Task> optTask = taskService.findById(id);
        if(!optTask.isPresent()){
            log.info("user {} attempts to get non-existing task by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetTaskByIdResponseDto
                    .fail(id, Error.TASK_NOT_FOUND));
        }
        Task task = optTask.get();
        UserRole role = getRelation(user, task.getList().getProject()).getRole();

        if(role.greaterOrEquals(UserRole.GUEST)){
            return ResponseEntity.ok(GetTaskByIdResponseDto.success(task));
        } else {
            log.info("user {} attempts to get task {} with role {}", user, task, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetTaskByIdResponseDto
                    .fail(id, Error.PERMISSIONS_NOT_GRANTED));
        }
    }



    @GetMapping("/in_list")
    public ResponseEntity<GetTaskByListResponseDto> getAllInList(@AuthenticationPrincipal UserSecurity authUser,
                                                                 @RequestParam(name = "id") Integer listId) {
        User user = authUser.getUser();
        Optional<TaskList> optList = listService.findById(listId);
        if(!optList.isPresent()) {
            log.info("user {} attempts to get tasks from non-existing list by id {}", user, listId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetTaskByListResponseDto
                    .fail(listId, Error.LIST_NOT_FOUND));
        }
        TaskList taskList = optList.get();
        UserRole role = getRelation(user, taskList.getProject()).getRole();
        if(role.greaterOrEquals(UserRole.GUEST)){
            return ResponseEntity.ok(GetTaskByListResponseDto.success(taskList));
        } else {
            log.info("user {} attempts to get tasks from list {} with role {}", user, taskList, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetTaskByListResponseDto
                    .fail(listId, Error.PERMISSIONS_NOT_GRANTED));
        }
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> create(@AuthenticationPrincipal UserSecurity authUser,
                                                  @RequestBody TaskRequestDto request) {
        User user = authUser.getUser();
        List<Error> errors = new ArrayList<>();
        Task task = new Task();
        setBasicFields(task, request);
        task.setCreator(user);
        task.setList(listService.findById(request.getListId()).orElseGet(() -> {
            log.info("user {} attempts to create a task in non-existing list by id {}",
                    user, request.getListId());
            errors.add(Error.LIST_NOT_FOUND);
            return null;
        }));
        if(request.getAssignedUserId() != null) {
            task.setAssignedUser(userService.findById(request.getAssignedUserId()).orElseGet(() -> {
                log.info("user {} attempts to create a task with non-existing assigned user by id {}",
                        user, request.getAssignedUserId());
                errors.add(Error.USER_NOT_FOUND);
                return null;
            }));
        }
        if(!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TaskResponseDto
                    .fail(errors));
        }
        UserRole creatorRole = getRelation(user, task.getList().getProject()).getRole();
        if(!creatorRole.greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} with role {} attempts to create a task in project {}",
                    user, creatorRole, task.getList().getProject());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TaskResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        UserRole assignedRole = getRelation(task.getAssignedUser(), task.getList().getProject()).getRole();
        if(task.getAssignedUser() != null && !assignedRole.greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} attempts to create a task with assigned user {} with role {}",
                    user, task.getAssignedUser(), assignedRole);
        }
        if(validator.validate(task)){
            Task savedTask = taskService.save(task);
            return ResponseEntity.ok(TaskResponseDto.success(savedTask));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TaskResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(@AuthenticationPrincipal UserSecurity authUser,
                                 @PathVariable("id") Integer id,
                                 @RequestBody TaskRequestDto request) {
        User user = authUser.getUser();
        Optional<Task> optTask = taskService.findById(id);
        if(!optTask.isPresent()){
            log.info("user {} attempts to update non-existing task by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TaskResponseDto
                    .fail(Error.TASK_NOT_FOUND));
        }
        Task task = optTask.get();
        List<Error> errors = new ArrayList<>();
        setBasicFields(task, request);
        task.setList(listService.findById(request.getListId()).orElseGet(() -> {
            log.info("user {} attempts to move a task {} to non-existing list by id {}",
                    user, task, request.getListId());
            errors.add(Error.LIST_NOT_FOUND);
            return null;
        }));
        if(request.getAssignedUserId() != null) {
            task.setAssignedUser(userService.findById(request.getAssignedUserId()).orElseGet(() -> {
                log.info("user {} attempts to set non-existing user by id {} assigned to a task {}",
                        user, request.getAssignedUserId(), task);
                errors.add(Error.USER_NOT_FOUND);
                return null;
            }));
        } else {
            task.setAssignedUser(null);
        }
        if(!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TaskResponseDto
                    .fail(errors));
        }
        UserRole role = getRelation(user, task.getList().getProject()).getRole();
        if(!role.greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} with role {} attempts to update a task {}",
                    user, role, task);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TaskResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        UserRole assignedRole = getRelation(task.getAssignedUser(), task.getList().getProject()).getRole();
        if(task.getAssignedUser() != null && !assignedRole.greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} attempts to set user {} with role {} assigned to a task {}",
                    user, task.getAssignedUser(), assignedRole, task);
        }
        if(validator.validate(task)){
            Task savedTask = taskService.save(task);
            return ResponseEntity.ok(TaskResponseDto.success(savedTask));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TaskResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskResponseDto> delete(@AuthenticationPrincipal UserSecurity authUser,
                                 @PathVariable("id") Integer id) {
        User user = authUser.getUser();
        Optional<Task> optTask = taskService.findById(id);
        if(!optTask.isPresent()){
            log.info("user {} attempts to update non-existing list by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TaskResponseDto
                    .fail(Error.TASK_NOT_FOUND));
        }
        Task task = optTask.get();
        UserRole role = getRelation(user, task.getList().getProject()).getRole();
        if(!role.greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} with role {} attempts to delete a task {}",
                    user, role, task);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TaskResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        taskService.delete(task);
        return ResponseEntity.ok(TaskResponseDto.success(task));
    }
}
