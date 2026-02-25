package com.boot.security.entity;

import lombok.*;
import jakarta.persistence.*; // Spring Boot 3 기준
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class DocumentObserver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Builder
    public DocumentObserver(Document document, User user) {
        this.document = document;
        this.user = user;
    }
}