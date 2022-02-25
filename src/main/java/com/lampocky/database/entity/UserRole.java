package com.lampocky.database.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

public enum UserRole{
    ADMIN(1),
    DEVELOPER(2),
    GUEST(3);

    private int id;

    UserRole(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static UserRole getById(int id){
        for(UserRole role: values()){
            if(role.id == id){
                return role;
            }
        }
        return null;
    }
}
