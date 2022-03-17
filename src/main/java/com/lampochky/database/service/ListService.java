package com.lampochky.database.service;

import com.lampochky.database.entity.TaskList;
import com.lampochky.database.repository.ListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListService extends AbstractService<TaskList> {
    private final ListRepository repository;

    @Autowired
    public ListService(ListRepository repository) {
        super(repository);
        this.repository = repository;
    }
}
