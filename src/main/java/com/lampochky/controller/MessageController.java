package com.lampochky.controller;

import com.lampochky.database.entity.*;
import com.lampochky.database.service.MessageService;
import com.lampochky.database.service.TaskService;
import com.lampochky.database.service.UserProjectService;
import com.lampochky.database.service.UserService;
import com.lampochky.dto.request.message.CreateMessageRequestDto;
import com.lampochky.dto.request.message.UpdateMessageRequestDto;
import com.lampochky.dto.response.message.GetMessageByIdResponseDto;
import com.lampochky.dto.response.message.GetMessagesInTaskResponseDto;
import com.lampochky.dto.response.message.MessageResponseDto;
import com.lampochky.config.security.UserSecurity;
import com.lampochky.validation.Error;
import com.lampochky.validation.MessageValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/data/message")
public class MessageController extends AbstractController{
    private final UserService userService;
    private final TaskService taskService;
    private final MessageService messageService;
    private final MessageValidator validator;

    public MessageController(UserProjectService userProjectService, UserService userService, TaskService taskService,
                             MessageService messageService) {
        super(userProjectService);
        this.userService = userService;
        this.taskService = taskService;
        this.messageService = messageService;
        validator = new MessageValidator();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetMessageByIdResponseDto> getById(@AuthenticationPrincipal UserSecurity userSecurity,
                                                             @PathVariable("id") Integer id) {
        User user = userSecurity.getUser();
        Optional<Message> optMessage = messageService.findById(id);
        if(!optMessage.isPresent()) {
            log.info("user {} attempts get non-existing message by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetMessageByIdResponseDto
                    .fail(id, Error.MESSAGE_NOT_FOUND));
        }
        Message message = optMessage.get();
        UserProject relation = getRelation(user, message.getTask().getList().getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            log.info("user {} with role {} attempts to get a message {}", user, relation.getRole(), message);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetMessageByIdResponseDto
                    .fail(id, Error.PERMISSIONS_NOT_GRANTED));
        }
        return ResponseEntity.ok(GetMessageByIdResponseDto.success(message));
    }

    @GetMapping("/in_task")
    public ResponseEntity<GetMessagesInTaskResponseDto> getAllInTask(@AuthenticationPrincipal UserSecurity userSecurity,
                                                                     @RequestParam("id") Integer id) {
        User user = userSecurity.getUser();
        Optional<Task> optTask = taskService.findById(id);
        if(!optTask.isPresent()) {
            log.info("user {} attempts get all messages in non-existing task by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(GetMessagesInTaskResponseDto
                    .fail(id, Error.TASK_NOT_FOUND));
        }
        Task task = optTask.get();
        UserProject relation = getRelation(user, task.getList().getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            log.info("user {} with role {} attempts to get all messages in a task {}",
                    user, relation.getRole(), task);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(GetMessagesInTaskResponseDto
                    .fail(id, Error.PERMISSIONS_NOT_GRANTED));
        }
        List<Message> messages = messageService.findAllInTaskSortByDate(task);
        return ResponseEntity.ok(GetMessagesInTaskResponseDto.success(task.getId(), messages));
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> create(@AuthenticationPrincipal UserSecurity userSecurity,
                                                     @RequestBody CreateMessageRequestDto request) {
        User user = userSecurity.getUser();
        Optional<Task> optTask = taskService.findById(request.getTaskId());
        if(!optTask.isPresent()) {
            log.info("user {} attempts to create a message in non-existing task by id {}",
                    user, request.getTaskId());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(MessageResponseDto
                    .fail(Error.TASK_NOT_FOUND));
        }
        Task task = optTask.get();
        UserProject relation = getRelation(user, task.getList().getProject());
        if(!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.GUEST)) {
            log.info("user {} with role {} attempts to create a message in a task {}",
                    user, relation.getRole(), task);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MessageResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        Message message = new Message(request.getText(), LocalDateTime.now(), user, task);
        if(validator.validate(message)) {
            message = messageService.save(message);
            return ResponseEntity.ok(MessageResponseDto.success(message));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(MessageResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponseDto> update(@AuthenticationPrincipal UserSecurity userSecurity,
                                                     @PathVariable("id") Integer id,
                                                     @RequestBody UpdateMessageRequestDto request) {
        User user = userSecurity.getUser();
        Optional<Message> optMessage = messageService.findById(id);
        if(!optMessage.isPresent()) {
            log.info("user {} attempts to update non-existing message by id {}",
                    user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MessageResponseDto
                    .fail(Error.MESSAGE_NOT_FOUND));
        }
        Message message = optMessage.get();
        if(!message.getUser().equals(user)) {
            log.info("user {} attempts to update a message {} created by {}",
                    user, message, message.getUser());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MessageResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        message.setText(request.getText());
        if(validator.validate(message)) {
            message = messageService.save(message);
            return ResponseEntity.ok(MessageResponseDto.success(message));
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(MessageResponseDto
                    .fail(validator.getErrors()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> delete(@AuthenticationPrincipal UserSecurity userSecurity,
                                                     @PathVariable("id") Integer id) {
        User user = userSecurity.getUser();
        Optional<Message> optMessage = messageService.findById(id);
        if(!optMessage.isPresent()) {
            log.info("user {} attempts to delete non-existing message by id {}", user, id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MessageResponseDto
                    .fail(Error.MESSAGE_NOT_FOUND));
        }
        Message message = optMessage.get();
        UserProject relation = getRelation(user, message.getTask().getList().getProject());
        if((!relation.getConfirmed() || !relation.getRole().greaterOrEquals(UserRole.ADMIN)) &&
                !message.getUser().equals(user)) {
            log.info("user {} with role {} attempts to delete a message {} created by {}",
                    user, relation.getRole(), message, message.getUser());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(MessageResponseDto
                    .fail(Error.PERMISSIONS_NOT_GRANTED));
        }
        messageService.delete(message);
        return ResponseEntity.ok(MessageResponseDto.success(message));
    }
}
