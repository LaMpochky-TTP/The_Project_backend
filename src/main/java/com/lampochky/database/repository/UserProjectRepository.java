package com.lampochky.database.repository;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.User;
import com.lampochky.database.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProjectRepository extends JpaRepository<UserProject, Integer> {
    List<UserProject> findByUserAndProject(User user, Project project);
}
