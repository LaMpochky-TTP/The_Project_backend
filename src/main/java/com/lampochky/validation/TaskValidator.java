package com.lampochky.validation;

import com.lampochky.database.entity.*;
import com.lampochky.database.service.UserService;

public class TaskValidator extends AbstractValidator<Task>{
    private final UserService userService;

    public TaskValidator(UserService userService) {
        this.userService = userService;
    }

    private void validateDates(Task task) {
        if(task.getDateToStart().isAfter(task.getDateToFinish())){
            errors.add(Error.DATE_ORDER_INVALID);
        }
    }

    public void validatePriority(Task task) {
        if(task.getPriority() == null){
            task.setPriority(0);
        }
    }

    public void validateDescription(Task task) {
        if(task.getDescription() == null){
            task.setDescription("");
        }
    }

    public void validateAssignedUser(Task task){
        User assignedUser = task.getAssignedUser();
        Project project = task.getList().getProject();

        if(assignedUser != null) {
            UserRole assignedRole = project.getUsers().stream()
                    .filter(u -> u.getUser().equals(assignedUser))
                    .findAny()
                    .orElseGet(() -> {
                        UserProject defaultRelation = new UserProject();
                        defaultRelation.setRole(UserRole.NO_RELATION);
                        return defaultRelation;
                    }).getRole();

            if(!assignedRole.greaterOrEquals(UserRole.DEVELOPER)){
                errors.add(Error.ASSIGNING_NOT_GRANTED_USER);
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        errors.clear();

        validateName(task.getName());
        validateDates(task);
        validatePriority(task);
        validateDescription(task);
        validateAssignedUser(task);

        return errors.isEmpty();
    }
}
