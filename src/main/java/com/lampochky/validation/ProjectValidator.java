package com.lampochky.validation;

import com.lampochky.database.entity.Project;

public class ProjectValidator extends AbstractValidator<Project> {
    @Override
    public boolean validate(Project project) {
        errors.clear();

        validateName(project.getName());

        return errors.isEmpty();
    }
}
