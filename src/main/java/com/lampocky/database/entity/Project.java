package com.lampocky.database.entity;

import javax.persistence.*;

@Entity
@Table(name = "project", schema = "lampochky")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @OneToMany(mappedBy = "project")
    private java.util.List<List> lists;

    @OneToMany(mappedBy = "project")
    private java.util.List<UserProject> users;

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

    public java.util.List<List> getLists() {
        return lists;
    }

    public void setLists(java.util.List<List> lists) {
        this.lists = lists;
    }

    public java.util.List<UserProject> getUsers() {
        return users;
    }

    public void setUsers(java.util.List<UserProject> users) {
        this.users = users;
    }
}
