package com.lampocky.database.entity;

import javax.persistence.*;

@Entity
@Table(name = "user_project", schema = "lampochky")
public class UserProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "role", nullable = false)
    @Convert(converter = UserRoleConvertor.class)
    private UserRole role;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
