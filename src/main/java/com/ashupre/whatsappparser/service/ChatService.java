package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.dto.ChatDTO;
import com.ashupre.whatsappparser.exceptions.ChatDeletionCountMismatchException;
import com.ashupre.whatsappparser.model.ChatCursor;
import com.ashupre.whatsappparser.dto.ChatResponsePaginated;
import com.ashupre.whatsappparser.model.Chat;
import com.ashupre.whatsappparser.model.ChatEntry;
import com.ashupre.whatsappparser.repository.ChatRepository;
import com.ashupre.whatsappparser.util.TimeFormatUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final DateTimeFormatter inputFormatter;

    private final DateTimeFormatter outputFormatter;

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper jacksonMapper;

    private final ChatRepository chatRepository;
    private final ZoneId asiaKolkataZoneId;

    /**
     * No need to create a mongoTemplate, springboot will automatically create a mongoTemplate for us using
     * the uri given in application.properties, we simply inject it
     * <p>
     * = new MongoTemplate(
     * new SimpleMongoClientDatabaseFactory(MongoClients.create("mongodb://localhost:27017"),
     * "whatsapp_parser")
     * );
     */
    public ResponseEntity<String> addChatsFromFile(MultipartFile file, String userId, String fileId) {
        List<ChatEntry> logEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            String name = null;
            String timestamp = null;
            StringBuilder messageBuilder = new StringBuilder();

            Pattern messagePattern = Pattern.compile(
                    // * \h? instead of a space before [ap]m
                    // * This allows for either a normal space or a non-breaking space (newer exports have small
                    // * gap between that is not a space -> non-breaking space).
                    // * similarly for beside -
                    "^(\\d{2}/\\d{2}/\\d{2}, \\d{1,2}:\\d{2}[\\h]?[ap]m)[\\h]-[\\h](.*?):\\s(.*)$"
            );
            while ((line = reader.readLine()) != null) {
                Matcher matcher = messagePattern.matcher(line);

                // regex match => new message
                if (matcher.matches()) {
                    // save prev message before processing new one
                    if (timestamp != null && name != null && !messageBuilder.isEmpty()) {
                        // convert non-breaking space (U+202F) to regular space as non-breaking spaces cant be
                        // parsed by LocalDateTime
                        timestamp = timestamp.replace("\u202F", " ");
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
                // convert non-breaking space (U+202F) to regular space as non-breaking spaces cant be
                // parsed by LocalDateTime
                timestamp = timestamp.replace("\u202F", " ");
                logEntries.add(new ChatEntry(LocalDateTime.parse(timestamp, inputFormatter),
                        name, messageBuilder.toString().trim()));
            }

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to read file: " + e.getMessage());
        }

        writeLogsToDB(logEntries, userId, fileId);
        return ResponseEntity.ok("added successfully");
    }


    public void writeLogsToDB(List<ChatEntry> logEntries, String userId, String fileId) {
        log.debug(" ====================================================================================== ");
        List<Chat> chatList;
        // parallelize if many chats
        if (logEntries.size() > 500) {
            chatList = logEntries.parallelStream()
                    .map(
                            entry -> Chat.builder()
                                    .message(entry.message())
                                    .userId(userId)
                                    .sender(entry.name())
                                    // convert timestamp to utc before storing in db
                                    .timestamp(TimeFormatUtil.localToUTC(entry.timestamp(), asiaKolkataZoneId))
                                    .fileDbId(fileId)
                                    .build()
                    ).toList();
        } else {
            chatList = logEntries.stream().map(
                    entry -> Chat.builder()
                            .message(entry.message())
                            .userId(userId)
                            .sender(entry.name())
                            // convert timestamp to utc before storing in db
                            .timestamp(TimeFormatUtil.localToUTC(entry.timestamp(), asiaKolkataZoneId))
                            .fileDbId(fileId)
                            .build()
            ).toList();
        }

        if (!chatList.isEmpty()) {
            saveChatsToDbAsync(chatList);
        }
    }

//    private CompletableFuture<Void> saveChatsToDbAsync(List<Chat> chatList) {
//        int insCount = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Chat.class)
//                .insert(chatList)
//                .execute()
//                .getInsertedCount();
//        if (insCount != chatList.size()) {
//            System.out.println("DB fucked up ********************************** ");
//        }
//        return CompletableFuture.completedFuture(null);
//    }

    private CompletableFuture<Void> saveChatsToDbAsync(List<Chat> chatList) {
        int batchSize = 100;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger totalInserted = new AtomicInteger(0);

        for (int i = 0; i < chatList.size(); i += batchSize) {
            List<Chat> batch = chatList.subList(i, Math.min(i + batchSize, chatList.size()));
            futures.add(CompletableFuture.runAsync(() -> {
                int insertedCount = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Chat.class)
                        .insert(batch).execute().getInsertedCount();
                totalInserted.addAndGet(insertedCount);
            }));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    if (totalInserted.get() != chatList.size()) {
                        System.err.println("NOT ALL INSERTED! ***************  " + totalInserted.get() + " out of " + chatList.size());
                    } else {
                        System.out.println("All chats saved successfully.");
                    }
                });
    }


    public ChatResponsePaginated getPaginatedChats(String userId, String fileId, ChatCursor prevCursor) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("fileDbId").is(fileId));
        log.debug("reached here in getPaginatedChats");

        if (prevCursor != null) {
            log.debug("prev cursor not null");
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
                                TimeFormatUtil.utcToLocal(chat.getTimestamp(), asiaKolkataZoneId)
                                        .format(outputFormatter)
                        )
                ).toList();
    }

    @Transactional
    public void deleteChats(String fileId) {
        long chatCount = chatRepository.countChatsByFileDbId(fileId);
        long deleteCount = chatRepository.deleteChatsByFileDbId(fileId);
        if (chatCount != deleteCount) {
            throw new ChatDeletionCountMismatchException("Deletion count mismatch for file " + fileId + ": "
                    + "Chat count - " + chatCount + " != " + deleteCount + " - deletion count");
        }
    }
}
