package com.ashupre.whatsappparser.repository;

import com.ashupre.whatsappparser.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, Long> {
    List<Chat> findByUserId(String userId);
}