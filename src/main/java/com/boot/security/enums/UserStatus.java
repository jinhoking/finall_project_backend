package com.boot.security.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("재직"),
    LEAVE("퇴사"),
    SUSPENDED("휴직"),
    VACATION("휴가"),        // 추가
    BUSINESS_TRIP("출장"),   // 추가
    MEETING("회의");         // 추가

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

}