package com.lampocky.database.service;

import com.lampocky.database.entity.Action;
import com.lampocky.database.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionService extends AbstractService<Action>{
    @Autowired
    public ActionService(ActionRepository repository) {
        super(repository);
    }
}
