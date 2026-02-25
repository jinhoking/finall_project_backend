package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Approvals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "approval_order")
    private Integer order;

    // 🚩 프론트/서비스 로직과 통일하기 위해 PENDING, APPROVED, REJECTED 사용 권장
    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, COMMENT

    private String comment;

    private LocalDateTime approvedAt;
}