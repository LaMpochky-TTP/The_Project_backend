package com.lampocky.database.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class UserRoleConvertor implements AttributeConverter<UserRole, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserRole userRole) {
        return userRole.getId();
    }

    @Override
    public UserRole convertToEntityAttribute(Integer id) {
        return UserRole.getById(id);
    }
}
