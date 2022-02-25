package com.lampocky.database.service;

import com.lampocky.database.entity.Tag;
import com.lampocky.database.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService extends AbstractService<Tag>{
    @Autowired
    public TagService(TagRepository repository) {
        super(repository);
    }
}
