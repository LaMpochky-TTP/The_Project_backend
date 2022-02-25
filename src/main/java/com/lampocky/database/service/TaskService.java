package com.lampocky.database.service;

import com.lampocky.database.entity.Task;
import com.lampocky.database.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService extends AbstractService<Task> {
    @Autowired
    public TaskService(TaskRepository repository) {
        super(repository);
    }
}
