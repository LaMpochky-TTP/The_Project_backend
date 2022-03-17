package com.lampochky.database.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "project", schema = "lampochky")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "project")
    private List<TaskList> taskLists;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "project")
    private List<UserProject> users;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "project")
    private List<Tag> tags;

    public Project(){}

    public Project(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

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

    public List<TaskList> getLists() {
        return taskLists;
    }

    public void setLists(List<TaskList> taskLists) {
        this.taskLists = taskLists;
    }

    public List<UserProject> getUsers() {
        return users;
    }

    public void setUsers(List<UserProject> users) {
        this.users = users;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project(" + id + ')';
    }
}
