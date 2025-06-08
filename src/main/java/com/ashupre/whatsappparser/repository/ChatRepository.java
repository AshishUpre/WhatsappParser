package com.ashupre.whatsappparser.repository;

import com.ashupre.whatsappparser.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, Long> {

    long deleteChatsByFileDbId(String fileDbId);

    long countChatsByFileDbId(String fileDbId);
}