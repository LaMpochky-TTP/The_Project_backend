package com.lampochky.database.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "task", schema = "lampochky")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "date_to_start", nullable = false)
    private LocalDate dateToStart;

    @Column(name = "date_to_finish", nullable = false)
    private LocalDate dateToFinish;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "description", length = 10000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false)
    private TaskList taskList;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "task")
    private List<Message> messages;

    @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "task")
    private List<Action> actions;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tag_task",
            joinColumns = {@JoinColumn(name = "task_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private List<Tag> tags;

    public Task(){}

    public Task(Integer id, String name, LocalDate dateToStart, LocalDate dateToFinish,
                Integer priority, String description, TaskList taskList, User assignedUser, User creator) {
        this.id = id;
        this.name = name;
        this.dateToStart = dateToStart;
        this.dateToFinish = dateToFinish;
        this.priority = priority;
        this.description = description;
        this.taskList = taskList;
        this.assignedUser = assignedUser;
        this.creator = creator;
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

    public LocalDate getDateToStart() {
        return dateToStart;
    }

    public void setDateToStart(LocalDate dateToStart) {
        this.dateToStart = dateToStart;
    }

    public LocalDate getDateToFinish() {
        return dateToFinish;
    }

    public void setDateToFinish(LocalDate dateToFinish) {
        this.dateToFinish = dateToFinish;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskList getList() {
        return taskList;
    }

    public void setList(TaskList taskList) {
        this.taskList = taskList;
    }

    public User getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(User assignedUser) {
        this.assignedUser = assignedUser;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task(" + id + ')';
    }
}
