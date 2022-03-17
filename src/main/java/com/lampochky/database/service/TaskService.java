package com.lampochky.database.service;

import com.lampochky.database.entity.Project;
import com.lampochky.database.entity.Task;
import com.lampochky.database.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService extends AbstractService<Task> {
    private final TaskRepository repository;
    @Autowired
    public TaskService(TaskRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Task> findByProject(Project project){
        return repository.findAllByProject(project);
    }
}
