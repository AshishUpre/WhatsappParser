package com.ashupre.whatsappparser.repository;

import com.ashupre.whatsappparser.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderId(String providerId);

}