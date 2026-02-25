package com.boot.security.controller;

import com.boot.security.dto.SecurityResponse;
import com.boot.security.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityApiController {

    // 랜덤 가짜 데이터 로직은 지우고, 진짜 저장소(Service)를 연결합니다.
    private final SecurityAuditService securityAuditService;

    @GetMapping("/dashboard")
    public ResponseEntity<SecurityResponse> getDashboardData() {

        // AOP가 열심히 모아둔 진짜 로그와 통계를 리액트 화면으로 쏴줍니다!
        SecurityResponse response = SecurityResponse.builder()
                .logs(securityAuditService.getLiveLogs())
                .dmlCount(securityAuditService.getDmlCount())
                .abnormalCount(securityAuditService.getAbnormalCount())
                .build();

        return ResponseEntity.ok(response);
    }
}