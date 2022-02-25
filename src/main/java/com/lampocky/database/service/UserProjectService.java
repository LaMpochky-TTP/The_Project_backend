package com.lampocky.database.service;

import com.lampocky.database.entity.UserProject;
import com.lampocky.database.repository.UserProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProjectService extends AbstractService<UserProject> {
    @Autowired
    public UserProjectService(UserProjectRepository repository) {
        super(repository);
    }
}
