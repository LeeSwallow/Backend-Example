package com.pnu.mongotest;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.pnu.mongotest.entity.ImageFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@SpringBootTest
public class MongoImageUploadTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoDatabaseFactory mongoDatabaseFactory;

    // 업로드. 메타데이터(description, uploadAt) 지정 가능
    @Test
    public void uploadImage() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/test-image.png");
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found!");
        }

        Document metaData = new Document();
        metaData.put("description", "샘플 이미지 파일");
        metaData.put("uploadAt", LocalDateTime.now().toString());
        metaData.put("contentType", "image/png");

        ObjectId fileId = gridFsTemplate.store(inputStream, "test-image.png", "image/png", metaData);
        System.out.println("Image uploaded to GridFS with ID: " + fileId.toHexString());
    }

    // 다운로드 - 파일을 바이트 배열로 읽기 (예: 저장 또는 검증용)
    @Test
    public void downloadImage() throws Exception {
        String fileIdString = "68a9b0f082865ba8f2bb89f5"; // 업로드 후 받은 ObjectId 문자열 넣기

        ObjectId fileId = new ObjectId(fileIdString);
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(where("_id").is(fileId)));

        if (gridFSFile == null) {
            System.out.println("File not found");
            return;
        }

        GridFsResource resource = gridFsTemplate.getResource(gridFSFile);

        try (InputStream inputStream = resource.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] fileData = baos.toByteArray();
            System.out.println("Downloaded file size: " + fileData.length);

            // 필요에 따라 파일로 저장 가능
             Files.write(Paths.get("downloaded-test-image.png"), fileData);
        }
    }

    // 삭제
    @Test
    public void deleteImage() {
        String fileIdString = "68a9b0f082865ba8f2bb89f5";

        ObjectId fileId = new ObjectId(fileIdString);
        gridFsTemplate.delete(new Query(where("_id").is(fileId)));
        System.out.println("Image deleted with ID: " + fileIdString);
    }

    // 전체 메타데이터 조회
    @Test
    public void listAllImages() {
        List<GridFSFile> files = new ArrayList<>();
        gridFsTemplate.find(new Query()).into(files);

        List<ImageFile> imageFiles = new ArrayList<>();
        for (GridFSFile file : files) {
            Document metaData = file.getMetadata();
            String description = metaData != null ? metaData.getString("description") : null;
            String contentType = metaData != null ? metaData.getString("contentType") : null;

            LocalDateTime uploadAt = null;
            if (metaData != null && metaData.containsKey("uploadAt")) {
                String dateStr = metaData.getString("uploadAt");
                if (dateStr != null) {
                    uploadAt = LocalDateTime.parse(dateStr);
                }
            }

            imageFiles.add(new ImageFile(
                    file.getObjectId(),
                    file.getFilename(),
                    contentType,
                    file.getLength(),
                    uploadAt,
                    description
            ));
        }

        imageFiles.forEach(img -> {
            System.out.println("ID: " + img.getId());
            System.out.println("Filename: " + img.getFilename());
            System.out.println("ContentType: " + img.getContentType());
            System.out.println("Size: " + img.getLength());
            System.out.println("UploadAt: " + img.getUploadAt());
            System.out.println("Description: " + img.getDescription());
            System.out.println("-----");
        });
    }

    // 메타데이터 수정 (예: description, uploadAt 필드 변경)
    @Test
    public void updateMetadata() {
        String fileIdString = "파일ID를_여기에_넣으세요";

        ObjectId fileId = new ObjectId(fileIdString);

        Document newMetaData = new Document();
        newMetaData.put("description", "새로운 설명으로 변경");
        newMetaData.put("uploadAt", LocalDateTime.now().toString());

        MongoDatabase mongoDatabase = mongoDatabaseFactory.getMongoDatabase();

        mongoDatabase.getCollection("fs.files")
                .updateOne(new Document("_id", fileId),
                        new Document("$set", new Document("metadata", newMetaData)));

        System.out.println("Metadata updated for file ID: " + fileIdString);
    }
}
