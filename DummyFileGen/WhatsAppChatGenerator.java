import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class WhatsAppChatGenerator {

    public static void main(String[] args) throws IOException {
        int NUMLINES = 500;

        String[] users = {"Yo", "Gurt", "Haiyo", "Me", "He", "Didn't", "No", "Not", "Me"};
        String[] messages = {
                "Gurt", "Hi Yo", "Why did you call me?", "He didn't call me", "No I didn't", "No, I'm Didnt",
                "I never said you are not", "Why would you say he is me", "Yeah exactly, he can't be me cause I'm me."
        };

        LocalDateTime startTime = LocalDateTime.of(2024, 6, 7, 13, 0); // Start time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy, h:mm a", Locale.ENGLISH);

        BufferedWriter writer = new BufferedWriter(new FileWriter("Dummy_chat.txt"));
        Random rand = new Random();

        int userIndex = 0;
        int messageIndex = 0;

        for (int i = 0; i < NUMLINES; i++) {
            int minutesToAdd = rand.nextInt(5) + 1;
            startTime = startTime.plusMinutes(minutesToAdd);

            String timestamp = startTime.format(formatter).toLowerCase();

            String user = users[userIndex];
            String message = messages[messageIndex];

            writer.write(timestamp + " - " + user + ": " + message);
            writer.newLine();

            userIndex = (userIndex + 1) % users.length;
            messageIndex = (messageIndex + 1) % messages.length;
        }

        writer.close();
        System.out.println("Chat file generated: Dummy_chat.txt");
    }
}
