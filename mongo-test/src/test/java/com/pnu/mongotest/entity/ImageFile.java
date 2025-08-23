package com.pnu.mongotest.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageFile {
    private ObjectId id;
    private String filename;
    private String contentType;
    private long length;
    private LocalDateTime uploadAt;
    private String description; // 메타데이터 예시
}
