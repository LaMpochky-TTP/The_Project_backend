package com.lampochky.validation;

import com.lampochky.database.entity.TaskList;

public class ListValidator extends AbstractValidator<TaskList> {
    @Override
    public boolean validate(TaskList taskList) {
        errors.clear();
        validateName(taskList.getName());
        return errors.isEmpty();
    }
}
