package com.lampochky.database.repository;

import com.lampochky.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    public List<User> findByUsername(String username);
    public List<User> findByEmail(String email);
    public List<User> findByUsernameOrEmail(String username, String email);
}
