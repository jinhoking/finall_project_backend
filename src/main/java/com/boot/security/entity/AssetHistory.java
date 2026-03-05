package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;      // 이력 제목 (예: 신규 입고, 수리 접수)
    private LocalDateTime eventDate;
    private String adminName;  // 처리자 성함

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;
}