package com.lampochky.database.repository;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("select p from Project p inner join p.users up where up.confirmed = true and up.user = ?1")
    List<Project> findAllByUser(User user);
}
