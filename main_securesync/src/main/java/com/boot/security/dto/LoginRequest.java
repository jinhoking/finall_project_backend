package com.boot.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String loginId;
    private String password;

    public String getUsername() {
        return loginId;
    }
}
