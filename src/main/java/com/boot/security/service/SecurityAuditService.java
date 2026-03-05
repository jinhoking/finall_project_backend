package com.boot.security.service;

import com.boot.security.dto.AuditLogDto; // 🌟 따로 만든 DTO 가져오기
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SecurityAuditService {

    // 전역에서 공유할 라이브 로그 리스트 (동시성 문제를 위해 동기화 리스트 사용)
    private final List<AuditLogDto> liveLogs = Collections.synchronizedList(new ArrayList<>());

    // 통계 누적 변수
    private int dmlCount = 0;
    private int abnormalCount = 0;

    public SecurityAuditService() {
        addLog("보안 관제 시스템(SIEM) 가동 시작...", "system");
    }

    // 아무 곳에서나 이 메서드를 부르면 관제판에 로그가 찍힙니다!
    public void addLog(String message, String type) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        liveLogs.add(new AuditLogDto(System.currentTimeMillis(), time, message, type));

        // 메모리 관리를 위해 최근 30개만 유지
        if (liveLogs.size() > 30) {
            liveLogs.remove(0);
        }

        if ("warning".equals(type)) abnormalCount++;
        else if ("system".equals(type) || "success".equals(type)) dmlCount++;
    }

    public List<AuditLogDto> getLiveLogs() {
        return new ArrayList<>(liveLogs); // 복사본 반환
    }

    public int getDmlCount() {
        return dmlCount;
    }

    public int getAbnormalCount() {
        return abnormalCount;
    }
}