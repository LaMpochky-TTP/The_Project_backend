package com.lampochky.database.entity;

public enum UserRole{
    ADMIN(1),
    DEVELOPER(2),
    GUEST(3),
    NO_RELATION(10); // not for saving

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

    public boolean greaterOrEquals(UserRole role) {
        return id <= role.getId();
    }
}
