package com.snackOverFlow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.snackOverFlow.entity.ProcessedMessage;

@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
    boolean existsByMessageId(String messageId);
}

