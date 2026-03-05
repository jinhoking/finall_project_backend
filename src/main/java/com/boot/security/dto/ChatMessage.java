package com.boot.security.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String roomId;
    private Long senderId;
    private String senderName;
    private Long receiverId; // 🌟 추가
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;  // 🌟 추가
}