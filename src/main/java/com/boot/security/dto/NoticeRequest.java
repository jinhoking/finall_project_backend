package com.boot.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeRequest {
    private String title;
    private String content;
    private String category; // "공지", "중요", "이벤트"
}