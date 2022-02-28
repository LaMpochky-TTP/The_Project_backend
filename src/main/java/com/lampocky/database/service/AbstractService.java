package com.lampocky.database.service;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public class AbstractService<T>{
    protected JpaRepository<T, Integer> repository;

    public AbstractService(JpaRepository<T, Integer> repository) {
        this.repository = repository;
    }

    public Optional<T> findById(Integer id){
        return repository.findById(id);
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public void delete(T entity){
        repository.delete(entity);
    }
}
