// com.boot.security.dto.UserJoinRequest.java (경로 확인)
package com.boot.security.dto;

import lombok.Data;

@Data
public class UserJoinRequest {
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Long deptId;
    private String position;
    private String joinDate; // 🌟 [추가] 가입 시 날짜를 받기 위한 필드
}