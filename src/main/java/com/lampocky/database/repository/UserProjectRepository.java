package com.lampocky.database.repository;

import com.lampocky.database.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProjectRepository extends JpaRepository<UserProject, Integer> {
}
