package com.lampochky.dto.response;

import com.lampochky.database.entity.*;
import com.lampochky.dto.response.list.ListDto;
import com.lampochky.dto.response.message.MessageDto;
import com.lampochky.dto.response.project.ProjectDto;
import com.lampochky.dto.response.tag.TagDto;
import com.lampochky.dto.response.task.TaskDto;
import com.lampochky.dto.response.user.UserDto;

import java.util.Collections;
import java.util.stream.Collectors;

public class DtoBuilder {
    private static ProjectDto buildProjectDto_base(Project project, UserRole role) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setRole(role);
        return dto;
    }

    public static ProjectDto buildShortProjectDto(Project project, UserRole role){
        if(project == null) {
            return null;
        }
        ProjectDto dto = buildProjectDto_base(project, role);
        dto.setLists(Collections.emptyList());
        dto.setUsers(Collections.emptyList());
        dto.setTags(Collections.emptyList());
        return dto;
    }

    public static ProjectDto buildFullProjectDto(Project project, UserRole role){
        if(project == null) {
            return null;
        }
        ProjectDto dto = buildProjectDto_base(project, role);
        dto.setLists(project.getLists().stream().map(DtoBuilder::buildFullListDto).collect(Collectors.toList()));
        dto.setUsers(project.getUsers().stream()
                .map(up -> buildUserDto(up.getUser(), up.getRole()))
                .collect(Collectors.toList()));
        dto.setTags(project.getTags().stream().map(DtoBuilder::buildTagDto).collect(Collectors.toList()));
        return dto;
    }

    public static ProjectDto buildProjectDto(Integer id, String name){
        ProjectDto dto = new ProjectDto();
        dto.setId(id);
        dto.setName(name);
        dto.setLists(Collections.emptyList());
        dto.setUsers(Collections.emptyList());
        return dto;
    }


    private static ListDto buildListDto_base(TaskList taskList) {
        ListDto dto = new ListDto();
        dto.setId(taskList.getId());
        dto.setName(taskList.getName());
        return dto;
    }

    public static ListDto buildShortListDto(TaskList taskList){
        if(taskList == null){
            return null;
        }
        ListDto dto = buildListDto_base(taskList);
        dto.setProjectId(taskList.getProject().getId());
        dto.setTasks(Collections.emptyList());
        return dto;
    }

    public static ListDto buildFullListDto(TaskList taskList){
        if(taskList == null){
            return null;
        }
        ListDto dto = buildListDto_base(taskList);
        dto.setProjectId(taskList.getProject().getId());
        dto.setTasks(taskList.getTasks().stream().map(DtoBuilder::buildFullTaskDto).collect(Collectors.toList()));
        return dto;
    }

    public static ListDto buildListDto(Integer id, String name, Integer projectId){
        ListDto dto = new ListDto();
        dto.setId(id);
        dto.setName(name);
        dto.setProjectId(projectId);
        dto.setTasks(Collections.emptyList());
        return dto;
    }


    private static TaskDto buildTaskDto_base(Task task){
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDateToStart(task.getDateToStart());
        dto.setDateToFinish(task.getDateToFinish());
        dto.setPriority(task.getPriority());
        dto.setDescription(task.getDescription());
        dto.setListId(task.getList().getId());
        dto.setAssignedUser(buildUserDto(task.getAssignedUser(), null));
        dto.setCreator(buildUserDto(task.getCreator(), null));
        return dto;
    }

    public static TaskDto buildFullTaskDto(Task task){
        if(task == null){
            return null;
        }
        TaskDto dto = buildTaskDto_base(task);
        dto.setTags(task.getTags().stream().map(DtoBuilder::buildTagDto).collect(Collectors.toList()));
        dto.setMessages(task.getMessages().stream().map(DtoBuilder::buildMessageDto).collect(Collectors.toList()));
        return dto;
    }

    public static TaskDto buildShortTaskDto(Task task) {
        if(task == null){
            return null;
        }
        TaskDto dto = buildTaskDto_base(task);
        dto.setTags(Collections.emptyList());
        dto.setMessages(Collections.emptyList());
        return dto;
    }


    public static UserDto buildUserDto(User user, UserRole role){
        if(user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(role);
        return dto;
    }


    public static TagDto buildTagDto(Tag tag){
        if(tag == null) {
            return null;
        }
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setProjectId(tag.getProject().getId());
        return dto;
    }


    public static MessageDto buildMessageDto(Message message){
        if(message == null){
            return null;
        }
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setUser(buildUserDto(message.getUser(), null));
        dto.setText(message.getText());
        dto.setDateTime(message.getDateTime());
        dto.setTaskId(message.getTask().getId());
        return dto;
    }
}
