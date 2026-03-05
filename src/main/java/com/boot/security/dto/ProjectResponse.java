package com.boot.security.dto;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String name;        // 리액트에서 p.name으로 쓰므로 필드명 매칭
    private String manager;
    private String description;
    private Integer progress;   // totalProgress
    private String status;
    private Integer currentStep;
    private String startDate;
    private Integer devCount;

    private Integer fe_progress; // frontendTotal
    private Integer be_progress; // backendTotal

    // 리액트가 기대하는 중첩 구조
    private Devs devs;
    private TechStack techStack;
    private List<IssueDto> issues;

    @Data @AllArgsConstructor
    public static class Devs {
        private String fe;
        private String be;
    }

    @Data @AllArgsConstructor @Builder
    public static class TechStack {
        private List<TechDetail> fe;
        private List<TechDetail> be;
    }

    @Data @AllArgsConstructor
    public static class TechDetail {
        private String name;
        private Integer progress;
    }

    @Data @AllArgsConstructor
    public static class IssueDto {
        private Long id;
        private String title;
        private String type;
        private String writer;
        private String date;
    }
}