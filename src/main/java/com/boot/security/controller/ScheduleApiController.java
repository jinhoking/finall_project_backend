package com.boot.security.controller;

import com.boot.security.dto.ScheduleRequest;
import com.boot.security.dto.ScheduleResponse;
import com.boot.security.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        String loginId = authentication.getName();
        return ResponseEntity.ok(scheduleService.createSchedule(request, loginId));
    }

    // 🌟 추가: 일정 수정(PUT) API 맵핑
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long id,
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok().build();
    }
}