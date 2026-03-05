package com.boot.security.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class DocumentCreateRequest {
    private String type;
    private String title;
    private String content;
    private String priority;
    private List<Long> approverIds;
    private List<Long> observerIds;
    private List<MultipartFile> files;
    private String comment;
}