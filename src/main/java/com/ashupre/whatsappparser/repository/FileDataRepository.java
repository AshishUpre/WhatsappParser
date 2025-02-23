package com.ashupre.whatsappparser.repository;

import com.ashupre.whatsappparser.model.FileData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileDataRepository extends MongoRepository<FileData, String> {
}