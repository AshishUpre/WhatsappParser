package com.ashupre.whatsappparser.service;

import com.ashupre.whatsappparser.model.DriveFileMetadata;
import com.ashupre.whatsappparser.model.FileData;
import com.ashupre.whatsappparser.repository.FileDataRepository;
import com.ashupre.whatsappparser.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileDataService {

    private final FileDataRepository fileDataRepository;

    private final MongoTemplate mongoTemplate;

    public void saveFileData(DriveFileMetadata metadata, String userId) {
        FileData fileData = FileData.builder().fileName(metadata.getFileName())
                .gdriveId(metadata.getDriveId())
                .userId(userId)
                .size(metadata.getSize())
                .uploadTime(
                        TimeFormatUtil
                                .localToUTC(
                                        LocalDateTime.parse(
                                                metadata.getUploadTime().toString()
                                        ), ZoneId.of("Asia/Kolkata")
                                )
                )
                .build();
        System.out.println("filedata : " + fileData);
        fileDataRepository.save(fileData);
    }

    public void deleteFileByDriveId(String fileDriveId) {
        fileDataRepository.deleteFileDataByGdriveId(fileDriveId);
    }

    // returns name and encrypted mongo id of all files of a user
    public List<Pair<String, String>> getAllFilesOfUser(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Order.desc("uploadTime")));
        List<FileData> files = mongoTemplate.find(query, FileData.class);
        return files.stream().map(fileData -> Pair.of(fileData.getFileName(),
                fileData.getGdriveId())).toList();
    }
}
