package com.lampochky.controller;

import com.lampochky.database.entity.*;
import com.lampochky.database.service.ProjectService;
import com.lampochky.database.service.TagService;
import com.lampochky.database.service.UserProjectService;
import com.lampochky.database.service.UserService;
import com.lampochky.dto.request.tag.CreateTagRequestDto;
import com.lampochky.dto.request.tag.UpdateTagRequestDto;
import com.lampochky.dto.response.tag.GetTagByIdResponseDto;
import com.lampochky.dto.response.tag.GetTagsInProjectResponseDto;
import com.lampochky.dto.response.tag.TagResponseDto;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.validation.Error;
import com.lampochky.validation.TagValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/data/tag")
public class TagController extends AbstractController{
    private final UserService userService;
    private final TagService tagService;
    private final ProjectService projectService;
    private final TagValidator validator;

    @Autowired
    public TagController(UserProjectService userProjectService, UserService userService, TagService tagService,
                         ProjectService projectService) {
        super(userProjectService);
        this.userService = userService;
        this.tagService = tagService;
        this.projectService = projectService;
        validator = new TagValidator();
    }

    @GetMapping("{id}")
    public ResponseEntity<GetTagByIdResponseDto> getById(@AuthenticationPrincipal UserSecurity userSecurity,
                                                         @PathVariable("id") Integer id){
        User user = userSecurity.getUser();
        Optional<Tag> optTag = tagService.findById(id);
        if(!optTag.isPresent()) {
            log.info("user {} attempts to get non-existing tag by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetTagByIdResponseDto
                    .fail(id, Error.TAG_NOT_FOUND));
        }
        Tag tag = optTag.get();
        UserProject relation = getRelation(user, tag.getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            log.info("user {} with role {} attempts to get tag {}", user, relation.getRole(), tag);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetTagByIdResponseDto
                    .fail(id, Error.PERMISSIONS_NOT_GRANTED));
        }
        return ResponseEntity.ok(GetTagByIdResponseDto.success(tag));
    }

    @GetMapping("/in_project")
    public ResponseEntity<GetTagsInProjectResponseDto> getInProject(@AuthenticationPrincipal UserSecurity userSecurity,
                                                                    @RequestParam("id") Integer projectId) {
        User user = userSecurity.getUser();
        Optional<Project> optProject = projectService.findById(projectId);
        if(!optProject.isPresent()) {
            log.info("user {} attempts to get tags in non-existing project by id {}", user, projectId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetTagsInProjectResponseDto
                    .fail(projectId, Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserProject relation = getRelation(user, project);
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            log.info("user {} with role {} attempts to get tags in project {}",
                    user, relation.getRole(), project);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetTagsInProjectResponseDto
                    .fail(projectId, Error.PERMISSIONS_NOT_GRANTED));
        }
        List<Tag> tags = project.getTags();
        return ResponseEntity.ok(GetTagsInProjectResponseDto.success(projectId, tags));
    }

    @PostMapping
    public ResponseEntity<TagResponseDto> create(@AuthenticationPrincipal UserSecurity usersecurity,
                                                 @RequestBody CreateTagRequestDto request){
        User user = usersecurity.getUser();
        Optional<Project> optProject = projectService.findById(request.getProjectId());
        if(!optProject.isPresent()) {
            log.info("user {} attempts to create a tag in non-existing project by id {}",
                    user, request.getProjectId());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TagResponseDto
                    .fail(Error.PROJECT_NOT_FOUND));
        }
        Project project = optProject.get();
        UserProject relation = getRelation(user, project);
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} with role {} attempts to create a tag in a project {}",
                    user, relation.getRole(), project);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TagResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        Tag tag = new Tag(request.getName(), project);
        if(validator.validate(tag)) {
            tag = tagService.save(tag);
            return ResponseEntity.ok(TagResponseDto.success(tag));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TagResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> create(@AuthenticationPrincipal UserSecurity userSecurity,
                                                 @PathVariable("id") Integer id,
                                                 @RequestBody UpdateTagRequestDto request){
        User user = userSecurity.getUser();
        Optional<Tag> optTag = tagService.findById(id);
        if(!optTag.isPresent()) {
            log.info("user {} attempts to update non-existing tag by id {}",
                    user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TagResponseDto
                    .fail(Error.TAG_NOT_FOUND));
        }
        Tag tag = optTag.get();
        UserProject relation = getRelation(user, tag.getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} with role {} attempts to update a tag {}",
                    user, relation.getRole(), tag);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TagResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        tag.setName(request.getName());
        if(validator.validate(tag)) {
            tag = tagService.save(tag);
            return ResponseEntity.ok(TagResponseDto.success(tag));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(TagResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagResponseDto> delete(@AuthenticationPrincipal UserSecurity userSecurity,
                                                 @PathVariable("id") Integer id) {
        User user = userSecurity.getUser();
        Optional<Tag> optTag = tagService.findById(id);
        if(!optTag.isPresent()) {
            log.info("user {} attempts to delete non-existing tag by id {}",
                    user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(TagResponseDto
                    .fail(Error.TAG_NOT_FOUND));
        }
        Tag tag = optTag.get();
        UserProject relation = getRelation(user, tag.getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.DEVELOPER)) {
            log.info("user {} with role {} attempts to delete a tag {}",
                    user, relation.getRole(), tag);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(TagResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        tagService.delete(tag);
        return ResponseEntity.ok(TagResponseDto.success(tag));
    }
}
