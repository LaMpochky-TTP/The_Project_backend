package com.lampochky.controller;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.User;
import com.lampochky.database.entity.UserProject;
import com.lampochky.database.entity.UserRole;
import com.lampochky.database.service.ProjectService;
import com.lampochky.database.service.UserProjectService;
import com.lampochky.database.service.UserService;
import com.lampochky.dto.request.project.InviteAnswerProjectRequestDto;
import com.lampochky.dto.request.project.InviteProjectRequestDto;
import com.lampochky.dto.request.project.ProjectRequestDto;
import com.lampochky.dto.response.project.*;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.validation.Error;
import com.lampochky.validation.ProjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/data/project")
public class ProjectController extends AbstractController{
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectValidator projectValidator = new ProjectValidator();

    @Autowired
    public ProjectController(UserProjectService userProjectService, ProjectService projectService,
                             UserService userService) {
        super(userProjectService);
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProjectByIdResponseDto> getById(@AuthenticationPrincipal UserSecurity authUser,
                                                             @PathVariable("id") Integer id){
        User user = authUser.getUser();
        Optional<Project> optProject = projectService.findById(id);
        if(!optProject.isPresent()){
            log.info("user {} attempts to get non-existing project by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetProjectByIdResponseDto
                    .fail(id, Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserRole role = getRelation(user, project).getRole();
        if(!role.greaterOrEquals(UserRole.GUEST)){
            log.info("user {} attempts to get project {} with role {}", user, project, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetProjectByIdResponseDto
                    .fail(id, Error.PERMISSIONS_NOT_GRANTED));
        } else {
            return ResponseEntity.ok(GetProjectByIdResponseDto.success(project, role));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<GetAllProjectsResponseDto> getAll(@AuthenticationPrincipal UserSecurity authUser){
        User user = authUser.getUser();
        List<Project> projects = projectService.findAllByUser(user);
        return ResponseEntity.ok(GetAllProjectsResponseDto.success(projects));
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDto> create(@AuthenticationPrincipal UserSecurity authUser,
                                                     @RequestBody ProjectRequestDto request){
        User user = authUser.getUser();
        Project project = new Project(null, request.getName());
        if(projectValidator.validate(project)){
            project = projectService.save(project);
            UserProject userProject = new UserProject(null, UserRole.ADMIN, true, user, project);
            userProjectService.save(userProject);
            return ResponseEntity.ok(ProjectResponseDto.success(project));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ProjectResponseDto
                    .fail(projectValidator.getErrors()));
        }
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<InviteProjectResponseDto> inviteUser(@AuthenticationPrincipal UserSecurity authUser,
                                                               @PathVariable("id") Integer projectId,
                                                               @RequestBody InviteProjectRequestDto request){
        User user = authUser.getUser();
        List<Error> errors = new ArrayList<>();
        String userIdentifier = request.getUserIdentifier();
        Project project = projectService.findById(projectId).orElseGet(() -> {
            log.info("{} invites user to non-existing project by id {}", user, projectId);
            errors.add(Error.PROJECT_NOT_FOUND);
            return null;
        });
        User inviter = userService.findByUsernameOrEmail(userIdentifier, userIdentifier).orElseGet(() -> {
            log.info("{} invites non-existing user with identifier {}",
                    user, request.getUserIdentifier());
            errors.add(Error.USER_NOT_FOUND);
            return null;
        });
        if(!errors.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(InviteProjectResponseDto
                    .fail(userIdentifier, projectId, errors));
        }
        UserRole role = getRelation(user, project).getRole();
        if(!role.greaterOrEquals(UserRole.ADMIN)){
            log.info("user {} attempts to invite user to project {} with role {}",
                    user, project, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(InviteProjectResponseDto
                    .fail(userIdentifier, projectId, Error.PERMISSIONS_NOT_GRANTED));
        }
        if(getRelation(inviter, project).getRole() != UserRole.NO_RELATION){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(InviteProjectResponseDto
                    .fail(userIdentifier, projectId, Error.USER_IS_ALREADY_MEMBER));
        }
        userProjectService.save(new UserProject(null, request.getRole(), false, inviter, project));
        return ResponseEntity.ok(InviteProjectResponseDto.success(userIdentifier, projectId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> update(@AuthenticationPrincipal UserSecurity authUser,
                                                     @PathVariable("id") Integer id,
                                                     @RequestBody ProjectRequestDto request){
        User user = authUser.getUser();
        Optional<Project> optProject = projectService.findById(id);
        if(!optProject.isPresent()){
            log.info("user {} attempts to update non-existing project bu id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ProjectResponseDto
                    .fail(Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        project.setName(request.getName());
        UserRole role = getRelation(user, project).getRole();
        if(!role.greaterOrEquals(UserRole.ADMIN)) {
            log.info("user {} attempts to update project {} with role {}",
                    user, project, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProjectResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        } else if(projectValidator.validate(project)){
            Project savedProject = projectService.save(project);
            return ResponseEntity.ok(ProjectResponseDto.success(savedProject));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ProjectResponseDto
                    .fail(projectValidator.getErrors()));
        }
    }

    @PutMapping("/{id}/invite")
    public ResponseEntity<InviteAnswerProjectResponseDto> inviterAnswer(
            @AuthenticationPrincipal UserSecurity authUser,
            @PathVariable("id") Integer projectId,
            @RequestBody InviteAnswerProjectRequestDto request){
        User user = authUser.getUser();
        Optional<Project> optProject = projectService.findById(projectId);
        if(!optProject.isPresent()){
            log.info("user {} attempts to confirm invition to non-existing project by id {} ", user, projectId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(InviteAnswerProjectResponseDto
                    .fail(projectId, Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserProject relation = getRelation(user, project);
        if(relation.getRole() == UserRole.NO_RELATION) {
            log.info("user {} attempts to confirm non-existing invition to project {}", user, project);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(InviteAnswerProjectResponseDto
                    .fail(project.getId(), Error.INVITION_NOT_FOUND));
        } else if (relation.getConfirmed()) {
            log.info("user {} attempts to confirm ({}) already confirmed invition to project {}",
                    user, relation.getConfirmed(), project);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(InviteAnswerProjectResponseDto
                    .fail(project.getId(), Error.USER_IS_ALREADY_MEMBER));
        }
        if(request.isConfirm()){
            relation.setConfirmed(true);
            userProjectService.save(relation);
        } else {
            userProjectService.delete(relation);
        }
        return ResponseEntity.ok(InviteAnswerProjectResponseDto
                .success(project.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> delete(@AuthenticationPrincipal UserSecurity authUser,
                                                     @PathVariable("id") Integer id){
        User user = authUser.getUser();
        Optional<Project> optProject = projectService.findById(id);
        if(!optProject.isPresent()){
            log.info("user {} attempts to delete non-existing project bu id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ProjectResponseDto
                    .fail(Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserProject relation = getRelation(user, project);
        if(!relation.getRole().greaterOrEquals(UserRole.ADMIN)) {
            log.info("user {} attempts to delete project {} with role {}",
                    user, project, relation.getRole());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProjectResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        projectService.delete(project);
        return ResponseEntity.ok(ProjectResponseDto.success(project));
    }
}
