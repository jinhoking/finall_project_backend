package com.boot.security.service;

import com.boot.security.dto.LoginRequest;
import com.boot.security.dto.PasswordResetRequest;
import com.boot.security.dto.UserJoinRequest;
import com.boot.security.entity.Department;
import com.boot.security.entity.User;
import com.boot.security.provider.JwtTokenProvider;
import com.boot.security.repository.DepartmentRepository;
import com.boot.security.repository.UserRepository;
import com.boot.security.role.UserRole;
import com.boot.security.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Spring Security 필수 구현 메서드
     * loginId를 통해 DB에서 유저를 찾아 Security용 UserDetails 객체로 변환합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLoginId())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();
    }

    /**
     * 로그인 로직
     * ID/PW 검증 후 JwtTokenProvider를 통해 토큰을 생성하여 반환합니다.
     */
    @Transactional
    public String login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.updateLastLogin();

        // 🚩 [중요] 수정된 Provider를 통해 아이디가 포함된 토큰을 생성합니다.
        return jwtTokenProvider.createToken(user.getLoginId(), user.getRole());
    }

    /**
     * 내 정보 조회 시 사용되는 메서드
     */
    @Transactional(readOnly = true)
    public User findByLoginId(String loginId) {
        // 디버깅을 위해 로그를 남깁니다. (null 여부 확인용)
        System.out.println("🚩 [조회 시도 중인 로그인 ID]: [" + loginId + "]");

        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 회원가입 로직
     */
    @Transactional
    public Long join(UserJoinRequest request) {
        if (userRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Department dept = null;
        if (request.getDeptId() != null) {
            dept = departmentRepository.findById(request.getDeptId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다."));
        }

        UserRole role = UserRole.ROLE_USER;
        if ("팀장".equals(request.getPosition()) || "본부장".equals(request.getPosition())) {
            role = UserRole.ROLE_MANAGER;
        } else if ("사장".equals(request.getPosition())) {
            role = UserRole.ROLE_ADMIN;
        }

        User user = User.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .department(dept)
                .position(request.getPosition())
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user).getId();
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!user.getName().equals(request.getName()) || !user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("입력하신 정보가 회원 정보와 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void updateUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // Entity에 setStatus 메서드나 updateStatus 메서드가 있다고 가정 (또는 Lombok @Setter)
        user.setStatus(newStatus);
        // user.updateStatus(newStatus); // 만약 별도 메서드를 쓴다면
    }

    @Transactional
    public void updateUser(Long id, Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 요청받은 데이터가 있을 때만 수정
        if (request.containsKey("name")) user.setName(request.get("name"));
        if (request.containsKey("email")) user.setEmail(request.get("email"));
        if (request.containsKey("phone")) user.setPhone(request.get("phone"));
        if (request.containsKey("position")) user.setPosition(request.get("position"));

        // 부서 수정이 필요한 경우 추가 로직 필요 (예: deptId 등)
    }
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 사원 정보를 찾을 수 없습니다. ID: " + id));
        userRepository.delete(user);
    }
}