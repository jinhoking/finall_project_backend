package com.boot.security.service;

import com.boot.security.dto.ScheduleRequest;
import com.boot.security.dto.ScheduleResponse;
import com.boot.security.entity.Schedule;
import com.boot.security.entity.User;
import com.boot.security.repository.ScheduleRepository;
import com.boot.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 🌟 1. 생성할 때 로그인한 사람의 부서, 이름, 직급을 찾아서 같이 저장해줌
    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Schedule schedule = Schedule.builder()
                .title(request.getTitle())
                .start(request.getStart())
                .color(request.getColor())
                .allDay(request.isAllDay())
                .deptName(user.getDepartment() != null ? user.getDepartment().getDeptName() : request.getDeptName())
                .writerId(user.getLoginId())
                .writerName(user.getName())
                .writerPosition(user.getPosition())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return convertToResponse(savedSchedule);
    }

    // 🌟 2. 새로 추가된 수정(PUT) 로직
    @Transactional
    public ScheduleResponse updateSchedule(Long id, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));

        schedule.setTitle(request.getTitle());
        schedule.setStart(request.getStart());
        schedule.setColor(request.getColor());

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return convertToResponse(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    // 🌟 프론트엔드로 나갈 때 작성자 정보를 포함해서 내보냄
    private ScheduleResponse convertToResponse(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .start(schedule.getStart())
                .color(schedule.getColor())
                .allDay(schedule.isAllDay())
                .deptName(schedule.getDeptName())
                .writerId(schedule.getWriterId())
                .writerName(schedule.getWriterName())
                .writerPosition(schedule.getWriterPosition())
                .build();
    }
}