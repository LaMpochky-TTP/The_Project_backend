package com.lampocky.database.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public class AbstractService<T>{
    protected JpaRepository<T, Integer> repository;
    protected Logger log;

    public AbstractService(JpaRepository<T, Integer> repository) {
        this.repository = repository;
        log = LogManager.getLogger(this.getClass());
    }

    public Optional<T> findById(Integer id){
        return repository.findById(id);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T save(T entity) {
        entity = repository.save(entity);
        log.info("{} saved", entity);
        return entity;
    }

    public void delete(T entity){
        repository.delete(entity);
        log.info("{} deleted", entity);
    }
}
