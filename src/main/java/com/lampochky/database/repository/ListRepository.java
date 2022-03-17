package com.lampochky.database.repository;

import com.lampochky.database.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListRepository extends JpaRepository<TaskList, Integer> {
}
