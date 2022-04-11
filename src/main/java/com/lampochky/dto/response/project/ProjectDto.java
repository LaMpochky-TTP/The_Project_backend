package com.lampochky.dto.response.project;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lampochky.database.entity.UserRole;
import com.lampochky.dto.response.list.ListDto;
import com.lampochky.dto.response.tag.TagDto;
import com.lampochky.dto.response.user.UserDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDto {
    private Integer id;
    private String name;
    private List<ListDto> lists;
    private List<UserDto> users;
    private List<TagDto> tags;
    private UserRole role;
    private Boolean confirmed;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ListDto> getLists() {
        return lists;
    }

    public void setLists(List<ListDto> lists) {
        this.lists = lists;
    }

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}
