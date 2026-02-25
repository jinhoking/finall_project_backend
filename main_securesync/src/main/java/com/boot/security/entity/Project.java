package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;
    private String managerName;
    private String description;
    private Integer totalProgress;
    private String status;
    private Integer currentStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    private LocalDateTime startDate;

    // 🌟 추가: 개발자 명단 (프론트에서 온 "개발사원1, 개발사원2" 저장)
    private String feDevs;
    private String beDevs;

    // 🌟 추가: 기술 스택 (프론트에서 온 ["React", "TS"]를 JSON 문자열로 저장)
    @Column(columnDefinition = "TEXT")
    private String feTechStack;
    @Column(columnDefinition = "TEXT")
    private String beTechStack;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProjectDetail detail;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "project_id")
    @Builder.Default
    private List<ProjectIssue> issues = new ArrayList<>();
}