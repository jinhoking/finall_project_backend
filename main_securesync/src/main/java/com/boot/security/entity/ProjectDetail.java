package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Project_Details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDetail {
    @Id
    private Long projectId; // Project의 ID를 그대로 PK로 사용

    @OneToOne
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    // 🖥️ Backend Group
    private Integer backendTotal;   // 백엔드 파트 평균 진행률
    private Integer javaProgress;   // Java/Spring 상세
    private Integer dbProgress;     // SQL/JPA 상세

    // 🎨 Frontend Group
    private Integer frontendTotal;  // 프론트엔드 파트 평균 진행률
    private Integer reactProgress;  // React 상세
    private Integer uiCssProgress;  // UI/Design 상세
}