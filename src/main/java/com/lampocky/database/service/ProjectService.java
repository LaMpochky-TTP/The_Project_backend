package com.lampocky.database.service;

import com.lampocky.database.entity.Project;
import com.lampocky.database.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService extends AbstractService<Project> {
    @Autowired
    public ProjectService(ProjectRepository repository) {
        super(repository);
    }
}
