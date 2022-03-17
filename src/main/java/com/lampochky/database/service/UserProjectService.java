package com.lampochky.database.service;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.User;
import com.lampochky.database.entity.UserProject;
import com.lampochky.database.repository.UserProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProjectService extends AbstractService<UserProject> {
    private final UserProjectRepository repository;
    @Autowired
    public UserProjectService(UserProjectRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public Optional<UserProject> findByUserAndProject(User user, Project project) {
        List<UserProject> list = repository.findByUserAndProject(user, project);
        if(list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0));
        }
    }

    @Override
    public void delete(UserProject entity) {
        entity.getUser().getProjects().remove(entity);
        entity.getProject().getUsers().remove(entity);
        super.delete(entity);
    }
}
