package com.lampocky.database.service;

import com.lampocky.database.entity.User;
import com.lampocky.database.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService extends AbstractService<User>{
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = repository.findByUsername(username);
        if(users.isEmpty()){
            log.debug("No user by username {} found", username);
            return Optional.empty();
        } else {
            log.debug("User found by username {}", username);
            return Optional.of(users.get(0));
        }
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = repository.findByEmail(email);
        if(users.isEmpty()){
            log.debug("No user by email {} found", email);
            return Optional.empty();
        } else {
            log.debug("User found by email {}", email);
            return Optional.of(users.get(0));
        }
    }

    public Optional<User> findByUsernameOrEmail(String username, String email){
        List<User> users = repository.findByUsernameOrEmail(username, email);
        if(users.isEmpty()){
            log.debug("No user by username {} or email {} found", username, email);
            return Optional.empty();
        } else {
            log.debug("User found by username {} or email {}", username, email);
            return Optional.of(users.get(0));
        }
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return super.save(user);
    }
}
