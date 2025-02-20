package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.model.Chat;
import com.ashupre.whatsappparser.model.ChatEntry;
import com.ashupre.whatsappparser.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final ChatRepository chatRepository;
    private final MongoTemplate mongoTemplate;
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

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            String name = null;
            String timestamp = null;
            StringBuilder messageBuilder = new StringBuilder();

            Pattern messagePattern = Pattern.compile("^(\\d{2}/\\d{2}/\\d{2}, \\d{1,2}:\\d{2} [ap]m) [-] (.*?): (.*)$");

            while ((line = reader.readLine()) != null) {
                Matcher matcher = messagePattern.matcher(line);

                // regex match => new message
                if (matcher.matches()) {
                    // save prev message before processing new one
                    if (timestamp != null && name != null && messageBuilder.length() > 0) {
                        logEntries.add(new ChatEntry(LocalDateTime.parse(timestamp, inputFormatter), name, messageBuilder.toString()));
                    }

                    timestamp = matcher.group(1);
                    name = matcher.group(2);
                    // as this is a new message, reset messageBuilder to remove prev message content
                    messageBuilder.setLength(0);
                    messageBuilder.append(matcher.group(3));
                } else {
                    // regex not matching => this is not a new message but continuation of prev one
                    if (messageBuilder.length() > 0) {
                        messageBuilder.append("\n");  // Preserve new lines for readability
                    }
                    messageBuilder.append(line);
                }
            }

            // last message
            if (timestamp != null && name != null && messageBuilder.length() > 0) {
                logEntries.add(new ChatEntry(LocalDateTime.parse(timestamp, inputFormatter), name, messageBuilder.toString().trim()));
            }

            reader.close();
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to read file: " + e.getMessage());
        }

        writeLogsToDB(logEntries, "log.txt", userId, fileDriveId);
        return ResponseEntity.ok("added successfully");
    }


    public void writeLogsToDB(List<ChatEntry> logEntries, String filePath, String userId, String fileDriveId) {
        List<Chat> chatList = new ArrayList<>();
        for (ChatEntry entry : logEntries) {
            Chat chat = new Chat();
            chat.setMessage(entry.message());
            chat.setTimestamp(Chat.localToUTC(entry.timestamp(), ZoneId.of("Asia/Kolkata")));
            chat.setSender(entry.name());
            chat.setUserId(userId);
            chat.setFileDriveId(fileDriveId);
//                chat.setFileName();

            chatList.add(chat);
        }
        System.out.println("logs written to " + filePath);
        if (!chatList.isEmpty()) {
            saveChatsToDbAsync(chatList);
        }
    }

    @Async
    protected CompletableFuture<Void> saveChatsToDbAsync(List<Chat> chatList) {
        mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, Chat.class)
                .insert(chatList)
                .execute();
        chatRepository.saveAll(chatList);
        return CompletableFuture.completedFuture(null);
    }


}
