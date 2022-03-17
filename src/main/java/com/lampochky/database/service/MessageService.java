package com.lampochky.database.service;

import com.lampochky.database.entity.Message;
import com.lampochky.database.entity.Task;
import com.lampochky.database.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService extends AbstractService<Message> {
    private final MessageRepository repository;

    @Autowired
    public MessageService(MessageRepository repository) {
        super(repository);
        this.repository = repository;
    }

    public List<Message> findAllInTaskSortByDate(Task task) {
        return repository.findAllByTask(task, Sort.by(Sort.Direction.ASC, "dateTime"));
    }
}
