package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String start; // FullCalendar 포맷 (YYYY-MM-DD)

    private String color;
    private boolean allDay;
    private String deptName;
    private String writerId; // 로그인 ID

    // 🌟 추가: 프론트 상세 모달에 띄울 작성자 이름과 직급
    private String writerName;
    private String writerPosition;

    @CreationTimestamp
    private LocalDateTime createdAt;
}