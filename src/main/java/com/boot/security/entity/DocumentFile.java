package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data// 연관 관계 편의 메서드 등을 위해 Setter 추가 권장
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName; // 사용자가 올린 파일명
    private String savedFilePath;    // 실제 저장된 경로 (C:/...)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document; // 어떤 기안서의 파일인지


}