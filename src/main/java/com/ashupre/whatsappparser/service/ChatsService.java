package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.model.ChatEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatsService {

    private final DateTimeFormatter inputFormatter;
    private final DateTimeFormatter outputFormatter;

    public ResponseEntity<String> addChatsFromFile(MultipartFile file) {
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
        writeLogsToDB(logEntries, "log.txt");
        return ResponseEntity.ok("added successfully");
    }


    public void writeLogsToDB(List<ChatEntry> logEntries, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (ChatEntry entry : logEntries) {
                String logMessage = "|timestamp : |" + outputFormatter.format(entry.timestamp()) +
                        "| name: " + entry.name().replaceAll("^\\s+", "⟶") +
                        "| message: " + entry.message().replaceAll("^\\s+", "⟶") + "|\n";
                writer.write(logMessage);
                writer.write(" ========================== \n\n");
            }
            System.out.println("logs written to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }


}
