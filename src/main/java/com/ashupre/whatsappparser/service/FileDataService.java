package com.ashupre.whatsappparser.service;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileDataService {

    private final FileDataRepository fileDataRepository;

    private final UserService userService;

    private final ChatService chatService;

    private final MongoTemplate mongoTemplate;

    private final ZoneId asiaKolkataZoneId;

    private FileData saveFileMetaDataToDB(File file, String userId) {
        FileData fileData = FileData.builder().fileName(file.getName())
                .userId(userId)
                .size(file.getTotalSpace())
                .uploadTime(TimeFormatUtil.localToUTC(LocalDateTime.now(), asiaKolkataZoneId))
                .build();

        return fileDataRepository.save(fileData);
    }

    @Transactional
    public void uploadFile(String email, MultipartFile file) {
        String userId = userService.getUserByEmail(email).getId();

        // file handling
        // /tmp/filename absolute path for the file (get by convFile.getAbsolutePath())
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        FileData fileData = saveFileMetaDataToDB(convFile, userId);

        userService.addFile(userId, convFile.getName(), fileData.getId());
        chatService.addChatsFromFile(file, userId, fileData.getId());
    }

    public void deleteFileById(String fileId) {
        fileDataRepository.deleteFileDataById(fileId);
    }

    // returns name and encrypted mongo id of all files of a user
    public List<Pair<String, String>> getAllFilesOfUser(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Order.desc("uploadTime")));
        List<FileData> files = mongoTemplate.find(query, FileData.class);
        return files.stream().map(fileData -> Pair.of(fileData.getFileName(),
                fileData.getId())).toList();
    }
}
