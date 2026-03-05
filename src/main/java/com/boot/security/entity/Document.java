package com.boot.security.entity;

import com.boot.security.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private String type;

    @Column
    private String priority;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"documents", "hibernateLazyInitializer", "handler"})
    private User writer;

    // 🌟 결재선 정렬: Approval 엔티티의 Java 필드명인 'order'를 기준으로 정렬합니다.
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("order ASC")
    @Builder.Default
    private List<Approval> approvals = new ArrayList<>();

    // 🌟 파일 연관 관계 추가: 이제 DB에서 Document를 조회할 때 파일 목록을 같이 가져올 수 있습니다.
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DocumentFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentObserver> observers = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = DocumentStatus.PENDING;
        }
    }
}