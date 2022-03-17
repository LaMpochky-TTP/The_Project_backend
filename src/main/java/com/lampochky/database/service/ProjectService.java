package com.lampochky.database.service;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.User;
import com.lampochky.database.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService extends AbstractService<Project> {
    private final ProjectRepository repository;

    @Autowired
    public ProjectService(ProjectRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Project> findAllByUser(User user){
        List<Project> result = repository.findAllByUser(user);
        return result;
    }
}
