package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.dto.ChatDTO;
import com.ashupre.whatsappparser.model.ChatCursor;
import com.ashupre.whatsappparser.dto.ChatResponsePaginated;
import com.ashupre.whatsappparser.model.Chat;
import com.ashupre.whatsappparser.model.ChatEntry;
import com.ashupre.whatsappparser.util.TimeFormatUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.query.Query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final DateTimeFormatter inputFormatter;

    private final DateTimeFormatter outputFormatter;

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper jacksonMapper;
    /**
     * No need to create a mongoTemplate, springboot will automatically create a mongoTemplate for us using
     * the uri given in application.properties, we simply inject it
     *
     *  = new MongoTemplate(
     *             new SimpleMongoClientDatabaseFactory(MongoClients.create("mongodb://localhost:27017"),
     *                     "whatsapp_parser")
     *     );
     */

    public ResponseEntity<String> addChatsFromFile(MultipartFile file, String userId, String fileDriveId) {
        List<ChatEntry> logEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            String name = null;
            String timestamp = null;
            StringBuilder messageBuilder = new StringBuilder();

            Pattern messagePattern = Pattern
                    .compile("^(\\d{2}/\\d{2}/\\d{2}, \\d{1,2}:\\d{2} [ap]m) [-] (.*?): (.*)$");

            while ((line = reader.readLine()) != null) {
                Matcher matcher = messagePattern.matcher(line);

                // regex match => new message
                if (matcher.matches()) {
                    // save prev message before processing new one
                    if (timestamp != null && name != null && !messageBuilder.isEmpty()) {
                        logEntries.add(new ChatEntry(LocalDateTime.parse(timestamp, inputFormatter),
                                name, messageBuilder.toString()));
                    }

                    timestamp = matcher.group(1);
                    name = matcher.group(2);
                    // as this is a new message, reset messageBuilder to remove prev message content
                    messageBuilder.setLength(0);
                    messageBuilder.append(matcher.group(3));
                } else {
                    // regex not matching => this is not a new message but continuation of prev one
                    if (!messageBuilder.isEmpty()) {
                        messageBuilder.append("\n");  // Preserve new lines for readability
                    }
                    messageBuilder.append(line);
                }
            }

            // last message
            if (timestamp != null && name != null && !messageBuilder.isEmpty()) {
                logEntries.add(new ChatEntry(LocalDateTime.parse(timestamp, inputFormatter),
                        name, messageBuilder.toString().trim()));
            }

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to read file: " + e.getMessage());
        }

        writeLogsToDB(logEntries, userId, fileDriveId);
        return ResponseEntity.ok("added successfully");
    }


    public void writeLogsToDB(List<ChatEntry> logEntries, String userId, String fileDriveId) {
        List<Chat> chatList = logEntries.stream().map(
                entry -> {
                    Chat chat = new Chat();
                    chat.setMessage(entry.message());
                    // convert timestamp to utc before storing in db
                    chat.setTimestamp(TimeFormatUtil.localToUTC(entry.timestamp(), ZoneId.of("Asia/Kolkata")));
                    chat.setSender(entry.name());
                    chat.setUserId(userId);
                    chat.setFileDriveId(fileDriveId);
                    // chat.setFileName();
                    return chat;
                }
        ).toList();

        if (!chatList.isEmpty()) {
            saveChatsToDbAsync(chatList);
        }
    }

    @Async
    protected CompletableFuture<Void> saveChatsToDbAsync(List<Chat> chatList) {
        mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Chat.class)
                .insert(chatList)
                .execute();
        return CompletableFuture.completedFuture(null);
    }

    public ChatResponsePaginated getPaginatedChats (String userId, String fileId, ChatCursor prevCursor) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("fileDriveId").is(fileId));
        System.out.println("reached here in getPaginatedChats");

        if (prevCursor != null) {
            System.out.println("prev cursor not null");
            Criteria paginationCriteria = new Criteria().orOperator(
                    Criteria.where("timestamp").lt(prevCursor.getTimestamp()),
                    Criteria.where("timestamp").is(prevCursor.getTimestamp()).and("_id").lt(prevCursor.getId())
            );

            query.addCriteria(paginationCriteria);
        }

        query.with(Sort.by(Sort.Order.desc("timestamp"), Sort.Order.desc("_id")));
        query.limit(100);

        List<Chat> chatList = mongoTemplate.find(query, Chat.class);
        List<ChatDTO> chatDTOList = getChatDTOList(chatList);

        ChatCursor cursor = null;
        if (!chatList.isEmpty()) {
            Chat lastChat = chatList.get(chatList.size() - 1);
            cursor = new ChatCursor(lastChat.getTimestamp(), lastChat.getId());
        }

        String cursorJSON;
        try {
            cursorJSON = jacksonMapper.writeValueAsString(cursor);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON exception " + e.getMessage());
        }

        return new ChatResponsePaginated(chatDTOList, cursorJSON);
    }

    private List<ChatDTO> getChatDTOList(List<Chat> chatList) {
        return chatList.isEmpty() ? List.of() : chatList.stream()
                .map(
                        chat -> new ChatDTO(
                                chat.getSender(),
                                chat.getMessage(),
                                // convert back to local after getting from DB, format as like in chat export text file
                                TimeFormatUtil.utcToLocal(chat.getTimestamp(), ZoneId.of("Asia/Kolkata"))
                                        .format(outputFormatter)
                        )
                ).toList();
    }

}
