package com.boot.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScheduleRequest {
    private String title;
    private String start;
    private String color;
    private boolean allDay;
    private String deptName; // 🌟 depName 오타 수정 완료
}