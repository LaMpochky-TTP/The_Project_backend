package com.lampocky.database.service;

import com.lampocky.database.entity.User;
import com.lampocky.database.repository.UserRepository;
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
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    public Optional<User> findByEmail(String username) {
        List<User> users = repository.findByEmail(username);
        if(users.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    public Optional<User> findByUsernameOrEmail(String username, String email){
        List<User> users = repository.findByUsernameOrEmail(username, email);
        if(users.isEmpty()){
            return Optional.empty();
        } else {
            return Optional.of(users.get(0));
        }
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }
}
