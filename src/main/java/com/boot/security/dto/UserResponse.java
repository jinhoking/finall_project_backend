package com.boot.security.dto;

import com.boot.security.entity.User;
import lombok.*;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String loginId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String role;
    private String position;
    private String deptName;
    private String status;
    private String joinDate;
    private String lastLoginAt; // 🌟 [추가] 실제 로그인 시간을 전달할 필드

    public UserResponse(User user) {
        this.id = user.getId();
        this.loginId = user.getLoginId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.address = user.getAddress();
        this.role = user.getRole().name();
        this.position = user.getPosition();
        this.status = user.getStatus() != null ? user.getStatus().name() : "ACTIVE";
        this.deptName = user.getDepartment() != null ? user.getDepartment().getDeptName() : "미배정";

        if (user.getJoinDate() != null) {
            this.joinDate = user.getJoinDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }

        // 🌟 [추가] 마지막 로그인 시간을 문자열로 변환
        if (user.getLastLoginAt() != null) {
            this.lastLoginAt = user.getLastLoginAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
    }
}