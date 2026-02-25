package com.boot.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ScheduleResponse {
    private Long id;
    private String title;
    private String start;
    private String color;
    private boolean allDay;
    private String writerId;
    private String deptName;

    private String writerName;
    private String writerPosition;
}