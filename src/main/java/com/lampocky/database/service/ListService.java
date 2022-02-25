package com.lampocky.database.service;

import com.lampocky.database.entity.List;
import com.lampocky.database.repository.ListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListService extends AbstractService<List> {
    @Autowired
    public ListService(ListRepository repository) {
        super(repository);
    }
}
