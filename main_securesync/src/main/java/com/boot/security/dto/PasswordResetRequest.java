package com.boot.security.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String loginId;      // 아이디
    private String name;         // 이름
    private String email;        // 이메일
    private String newPassword;  // 변경할 새 비밀번호
}