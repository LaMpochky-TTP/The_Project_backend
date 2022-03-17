package com.lampochky.database.repository;

import com.lampochky.database.entity.Message;
import com.lampochky.database.entity.Task;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findAllByTask(Task task, Sort sort);
}
