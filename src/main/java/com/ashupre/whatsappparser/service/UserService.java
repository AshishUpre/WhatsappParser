package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.exceptions.UserNotFoundException;
import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void addFile(String userId, String fileName, String driveId) {
        System.out.println("\n in add file received file id: " + driveId);
        System.out.println("user id: " + userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getFiles() == null) {
            user.setFiles(new ArrayList<>());
        }

        if (!user.hasFileDriveId(driveId)) {
            List<User.FileMetadata> files = user.getFiles();
            files.add(new User.FileMetadata(fileName, driveId, null));
            user.setFiles(files);
        }

        userRepository.save(user);
    }

    // Get a user by email
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        return user.get();
    }

    public void deleteFileByDriveId(String userEmail, String driveId) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found"));

        // understand - difference between failing fast (by throwing NPE) and handling nulls
        // here userFiles being null is valid (not a bug) --- happens when user has no files
        // so we have to handle that
        /**
         * Behavior	Without Graceful Handling	-------------------- With Graceful Handling (Optional.ofNullable)
         * getFiles() returns null	NullPointerException	---------------  Treated as empty list ✅
         * You loop over it	Crashes app  --------------	Simply skips loop (nothing to do)
         * You filter/map on it	Crashes app ----------------	Works fine — stream on empty list
         * You conditionally check	Need to write if != null checks	------------------ No need — safe to use
         */
        List<User.FileMetadata> userFiles = Optional.ofNullable(user.getFiles())
                .orElse(Collections.emptyList());

        List<User.FileMetadata> updatedFiles = userFiles.stream().filter(
                        fileMetadata -> !fileMetadata.driveId().equals(driveId)
                ).toList();

        if (userFiles.size() == updatedFiles.size()) {
            throw new IllegalArgumentException("User doesn't have file with given drive id");
        }

        user.setFiles(updatedFiles);
        userRepository.save(user);
    }

    // Get a user by ID
    public User getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return user;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Delete a user by ID
    @Transactional
    public void deleteUserById(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User does not exist");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }

    // Helper method to process profile picture
    private String processProfilePicture(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process profile picture", e);
        }
    }
}
