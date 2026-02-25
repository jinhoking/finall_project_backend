package com.boot.security.controller;


import com.boot.security.annotation.AuditLog;
import com.boot.security.dto.LoginRequest;
import com.boot.security.dto.UserJoinRequest;
import com.boot.security.dto.UserResponse;
import com.boot.security.entity.User;
import com.boot.security.enums.UserStatus;
import com.boot.security.repository.UserRepository;
import com.boot.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.boot.security.service.SecurityAuditService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SecurityAuditService securityAuditService;


    @PostMapping("/join")
    @AuditLog(action = "회원가입이 확인 되었습니다.", type = "success")
    public ResponseEntity<String> join(@RequestBody UserJoinRequest request) {
        userService.join(request);
        return ResponseEntity.ok("회원가입 완료!!");
    }

    @PostMapping("/login")
    @AuditLog(action = "로그인이 확인 되었습니다.", type = "success")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

            // 기존의 로그인 처리 로직 (예시)
            String token = userService.login(request);
            return ResponseEntity.ok(token);

    }
    // UserApiController.java (파일 위치 확인: controller 폴더)
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal(expression = "username") String loginId) {
        User user = userService.findByLoginId(loginId);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .position(user.getPosition())
                .deptName(user.getDepartment() != null ? user.getDepartment().getDeptName() : "미배정")
                .status(user.getStatus().name())
                // 🌟 [추가] 빌더에도 joinDate를 넣어줘야 본인 정보 볼 때 날짜가 뜹니다.
                .joinDate(user.getJoinDate() != null ? user.getJoinDate().toString() : null)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(UserResponse::new) // 이미 만드신 UserResponse 생성자 활용
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.boot.security.dto.PasswordResetRequest request) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //사원 상태값
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String statusStr = request.get("status");
            if (statusStr == null) return ResponseEntity.badRequest().body("상태 값이 누락되었습니다."); // 🌟 NPE 방지

            UserStatus newStatus = UserStatus.valueOf(statusStr);
            userService.updateUserStatus(id, newStatus);
            return ResponseEntity.ok("상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 상태 값입니다: " + request.get("status"));
        } catch (Exception e) {
            e.fillInStackTrace(); // 🌟 서버 콘솔에서 진짜 에러 원인(Stack Trace)을 보기 위해 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경 실패: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            // 서비스에서 수정 로직 수행
            userService.updateUser(id, request);
            return ResponseEntity.ok("사원 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            e.fillInStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("수정 실패: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')") // 🌟 관리자와 팀장만 가능
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id); // 서비스의 deleteUser 호출
            return ResponseEntity.ok("사원 삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }
}


