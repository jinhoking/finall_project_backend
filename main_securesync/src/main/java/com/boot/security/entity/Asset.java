package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String assetNumber;

    private String name;
    private String category;
    private String status;
    private String sn;
    private Long price;
    private String location;
    private String warranty;
    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User holder; // (기존) DB 회원 연결용

    // 🌟 [추가됨] 화면에서 입력한 이름 그대로 저장할 컬럼
    private String holderName;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssetHistory> history = new ArrayList<>();
}