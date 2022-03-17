package com.lampochky.database.service;

import com.lampochky.database.entity.Action;
import com.lampochky.database.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionService extends AbstractService<Action>{
    @Autowired
    public ActionService(ActionRepository repository) {
        super(repository);
    }
}
