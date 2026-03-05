package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomId;
    private Long senderId;
    private String senderName;
    private Long receiverId; // 🌟 추가: 수신자 ID

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    @Builder.Default
    private boolean isRead = false; // 🌟 추가: 읽음 상태
}