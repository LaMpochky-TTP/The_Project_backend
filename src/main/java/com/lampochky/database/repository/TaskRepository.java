package com.lampochky.database.repository;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("select t from Task t inner join t.taskList l where l.project = ?1")
    List<Task> findAllByProject(Project project);
}
