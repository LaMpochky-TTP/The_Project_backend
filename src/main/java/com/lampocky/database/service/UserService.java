package com.lampocky.database.service;

import com.lampocky.database.entity.User;
import com.lampocky.database.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractService<User>{
    @Autowired
    public UserService(UserRepository repository) {
        super(repository);
    }
}
