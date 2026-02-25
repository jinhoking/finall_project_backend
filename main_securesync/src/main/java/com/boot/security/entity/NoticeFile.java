package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(nullable = false)
    private String originalFileName; // 사용자가 올린 원본 파일명

    @Column(nullable = false)
    private String savedFileName; // 서버(S3/로컬)에 저장된 고유 파일명 (UUID 등)

    @Column(nullable = false)
    private String filePath; // 저장된 실제 경로

    @Column(nullable = false)
    private long fileSize; // 파일 크기

    // 🌟 갤러리형 게시판의 핵심! (대표 커버 이미지인지, 일반 첨부파일인지 구분)
    @Column(nullable = false)
    private boolean isCover;
}