package com.lampochky.controller;

import com.lampochky.database.entity.*;
import com.lampochky.database.service.ListService;
import com.lampochky.database.service.ProjectService;
import com.lampochky.database.service.UserProjectService;
import com.lampochky.dto.request.list.CreateListRequestDto;
import com.lampochky.dto.request.list.UpdateListRequestDto;
import com.lampochky.dto.response.list.ListResponseDto;
import com.lampochky.dto.response.list.GetListByIdResponseDto;
import com.lampochky.dto.response.list.GetListsInProjectResponseDto;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.validation.Error;
import com.lampochky.validation.ListValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/data/list")
public class ListController extends AbstractController{
    private final ProjectService projectService;
    private final ListService listService;
    private final ListValidator validator;

    @Autowired
    public ListController(UserProjectService userProjectService, ProjectService projectService,
                          ListService listService) {
        super(userProjectService);
        this.projectService = projectService;
        this.listService = listService;
        this.validator = new ListValidator();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetListByIdResponseDto> getById(@AuthenticationPrincipal UserSecurity authUser,
                                                          @PathVariable("id") Integer id) {
        User user = authUser.getUser();
        Optional<TaskList> optList = listService.findById(id);
        if(!optList.isPresent()) {
            log.info("user {} attempts to get non-existing list by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetListByIdResponseDto
                    .fail(id, Error.LIST_NOT_FOUND));
        }
        TaskList taskList = optList.get();
        UserProject relation = getRelation(user, taskList.getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            log.info("user {} attempts to get list {} with role {}", user, taskList, relation.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetListByIdResponseDto
                    .fail(id, Error.PERMISSIONS_NOT_GRANTED));
        }
        return ResponseEntity.ok(GetListByIdResponseDto.success(taskList));
    }

    @GetMapping("/in_project")
    public ResponseEntity<GetListsInProjectResponseDto> getAllInProject(
            @AuthenticationPrincipal UserSecurity authUser,
            @RequestParam(name = "id") Integer projectId){
        User user = authUser.getUser();
        Optional<Project> optProject = projectService.findById(projectId);

        if(!optProject.isPresent()){
            log.info("user {} attempts to get lists of non-existing project by id {}", user, projectId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetListsInProjectResponseDto
                    .fail(projectId, Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserProject relation = getRelation(user, project);
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetListsInProjectResponseDto
                    .fail(projectId, Error.PERMISSIONS_NOT_GRANTED));
        }
        List<TaskList> taskLists = project.getLists();
        return ResponseEntity.ok(GetListsInProjectResponseDto.success(projectId, taskLists));
    }

    @PostMapping
    public ResponseEntity<ListResponseDto> create(@AuthenticationPrincipal UserSecurity authUser,
                                                  @RequestBody CreateListRequestDto request) {
        User user = authUser.getUser();
        Optional<Project> optProject = projectService.findById(request.getProjectId());
        if(!optProject.isPresent()) {
            log.info("user {} attempts to create list in non-existing project by id {}",
                    user, request.getProjectId());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ListResponseDto
                    .fail(Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserProject relation = getRelation(user, project);
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.ADMIN)) {
            log.info("user {} attempts to create list in project {} with role {}",
                    user, project, relation.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ListResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));

        }
        TaskList taskList = new TaskList(null, request.getName(), project);
        if(validator.validate(taskList)) {
            taskList = listService.save(taskList);
            return ResponseEntity.ok(ListResponseDto.success(taskList));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ListResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListResponseDto> update(@AuthenticationPrincipal UserSecurity authUser,
                                                  @PathVariable("id") Integer id,
                                                  @RequestBody UpdateListRequestDto request) {
        User user = authUser.getUser();
        Optional<TaskList> optTaskList = listService.findById(id);
        if(!optTaskList.isPresent()) {
            log.info("user {} attempts to modify non-existing list by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ListResponseDto
                    .fail(Error.LIST_NOT_FOUND));
        }
        TaskList taskList = optTaskList.get();
        UserRole role = getRelation(user, taskList.getProject()).getRole();
        if(!role.greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} attempts to modify list {} with role {}", user, taskList, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ListResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        taskList.setName(request.getName());
        if(validator.validate(taskList)){
            taskList = listService.save(taskList);
            return ResponseEntity.ok(ListResponseDto.success(taskList));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ListResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ListResponseDto> delete(@AuthenticationPrincipal UserSecurity authUser,
                                                  @PathVariable("id") Integer id) {
        User user = authUser.getUser();
        Optional<TaskList> optTaskList = listService.findById(id);
        if(!optTaskList.isPresent()) {
            log.info("user {} attempts to delete non-existing list by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ListResponseDto
                    .fail(Error.LIST_NOT_FOUND));
        }
        TaskList taskList = optTaskList.get();
        UserRole role = getRelation(user, taskList.getProject()).getRole();
        if(!role.greaterOrEquals(UserRole.ADMIN)) {
            log.info("user {} attempts to delete list {} with role {}", user, taskList, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ListResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        listService.delete(taskList);
        return ResponseEntity.ok(ListResponseDto.success(taskList));
    }
}
