package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.exceptions.UserNotFoundException;
import com.ashupre.whatsappparser.model.User;
import com.ashupre.whatsappparser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Create a new user with an optional profile picture
    public User createUser(String username, String email, String password, MultipartFile file) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        if (file != null && !file.isEmpty()) {
            user.setProfilePic(processProfilePicture(file));
        }

        return userRepository.save(user);
    }

    @Transactional
    public User addFile(String userId, String fileId) {
        System.out.println("\n in add file received file id: " + fileId);
        System.out.println("user id: " + userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getFileIds() == null) {
            List<String> fileIds = new ArrayList<>();
            fileIds.add(fileId);
            user.setFileIds(fileIds);
        } else if (!user.getFileIds().contains(fileId)) {
            user.getFileIds().add(fileId);
        }

        return userRepository.save(user);
    }

    public List<String> getAllFilesOfUser(String userId) {
        System.out.println("reached user service, get all files of user");
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        System.out.println("user: " + user);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user.getFileIds();
    }

    // Update an existing user's profile picture
    public User updateProfilePic(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (file != null && !file.isEmpty()) {
            user.setProfilePic(processProfilePicture(file));
        }

        return userRepository.save(user);
    }

    // Get a user by email
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
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
    public void deleteUserById(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User does not exist");
        }
        userRepository.deleteById(userId);
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

    public boolean authenticateUser(String userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user != null && user.getPassword().equals(password)) {
            return true;
        }
        return false;
    }

    public String authenticateUserAndGetToken(String email, String password) {
        if (authenticateUser(email, password)) {
            return Base64.getEncoder().encodeToString((email + ":" + password).getBytes()); // Simple token generation
        }
        return null;
    }
//    todo: get all files of user, integration between files table and users table (first check if files table actually needed or not)
}
