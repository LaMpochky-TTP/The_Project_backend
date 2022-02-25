package com.lampocky.database.service;

import com.lampocky.database.entity.Message;
import com.lampocky.database.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService extends AbstractService<Message> {
    @Autowired
    public MessageService(MessageRepository repository) {
        super(repository);
    }
}
