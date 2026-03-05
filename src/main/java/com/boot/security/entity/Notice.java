package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String category; // "공지", "중요", "이벤트"

    @Column(nullable = false, columnDefinition = "integer default 0")
    @Builder.Default
    private int viewCount = 0; // 조회수

    // 작성자 (User 엔티티와 연관관계) - 부서, 직급, 이름 정보를 가져오기 위함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 첨부파일 리스트 (게시글 삭제 시 파일도 함께 삭제)
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NoticeFile> files = new ArrayList<>();

    // 댓글 리스트 (게시글 삭제 시 댓글도 함께 삭제)
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NoticeComment> comments = new ArrayList<>();

    // 조회수 증가 편의 메서드
    public void addViewCount() {
        this.viewCount++;
    }
}