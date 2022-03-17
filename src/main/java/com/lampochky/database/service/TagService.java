package com.lampochky.database.service;

import com.lampochky.database.entity.Tag;
import com.lampochky.database.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService extends AbstractService<Tag>{
    private final TagRepository repository;

    @Autowired
    public TagService(TagRepository repository) {
        super(repository);
        this.repository = repository;
    }
}
