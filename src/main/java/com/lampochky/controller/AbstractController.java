package com.lampochky.controller;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.User;
import com.lampochky.database.entity.UserProject;
import com.lampochky.database.entity.UserRole;
import com.lampochky.database.service.UserProjectService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractController {
    protected final Logger log;
    protected final UserProjectService userProjectService;

    public AbstractController(UserProjectService userProjectService) {
        this.userProjectService = userProjectService;
        this.log = LogManager.getLogger(getClass());
    }

    protected UserProject getRelation(User user, Project project) {
        return userProjectService.findByUserAndProject(user, project).orElseGet(() -> {
                    UserProject defaultRelation = new UserProject();
                    defaultRelation.setConfirmed(false);
                    defaultRelation.setRole(UserRole.NO_RELATION);
                    return defaultRelation;
                });
    }
}
