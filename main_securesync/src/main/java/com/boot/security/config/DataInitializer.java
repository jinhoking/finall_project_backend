package com.boot.security.config;

import com.boot.security.entity.Department;
import com.boot.security.entity.User;
import com.boot.security.enums.UserStatus;
import com.boot.security.repository.DepartmentRepository;
import com.boot.security.repository.UserRepository;
import com.boot.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. 기존 데이터가 있으면 초기화하지 않음 (중복 방지)
        if (userRepository.count() > 0) {
            System.out.println("이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("기초 데이터 생성 시작...");

        // 2. 부서 생성
        Department main = saveDept("본사", null);
        Department strategyDiv = saveDept("전략기획본부", main);
        Department devDiv = saveDept("개발지원본부", main);

        Department hrTeam = saveDept("인사팀", strategyDiv);
        Department mgtTeam = saveDept("경영지원팀", strategyDiv);
        Department devTeam = saveDept("개발팀", devDiv);
        Department secTeam = saveDept("보안팀", devDiv);

        // 3. 사용자 생성 (비밀번호: 1234)
        String pw = passwordEncoder.encode("1234");

        // [관리자]
        saveUser("admin", pw, "관리자", null, "관리자", UserRole.ROLE_ADMIN);

        // [본부장]
        saveUser("head_strategy", pw, "전략본부장", strategyDiv, "본부장", UserRole.ROLE_MANAGER);
        saveUser("head_dev", pw, "개발본부장", devDiv, "본부장", UserRole.ROLE_MANAGER);

        // [인사팀]
        saveUser("hr_leader", pw, "인사팀장", hrTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("hr_cha1", pw, "인사차장", hrTeam, "차장", UserRole.ROLE_USER);
        saveUser("hr_kwa1", pw, "인사과장", hrTeam, "과장", UserRole.ROLE_USER);
        saveUser("hr_daeri1", pw, "인사대리", hrTeam, "대리", UserRole.ROLE_USER);
        saveUser("hr_sawon1", pw, "인사사원", hrTeam, "사원", UserRole.ROLE_USER);

        // [경영지원팀]
        saveUser("mgt_leader", pw, "경영팀장", mgtTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("mgt_sawon1", pw, "경영사원", mgtTeam, "사원", UserRole.ROLE_USER);

        // [개발팀]
        saveUser("dev_leader", pw, "개발팀장", devTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("dev_cha1", pw, "개발차장", devTeam, "차장", UserRole.ROLE_USER);
        saveUser("dev_kwa1", pw, "개발과장", devTeam, "과장", UserRole.ROLE_USER);
        saveUser("dev_daeri1", pw, "개발대리", devTeam, "대리", UserRole.ROLE_USER);
        saveUser("dev_sawon1", pw, "개발사원", devTeam, "사원", UserRole.ROLE_USER);

        // [보안팀]
        saveUser("sec_leader", pw, "보안팀장", secTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("sec_sawon1", pw, "보안사원", secTeam, "사원", UserRole.ROLE_USER);

        System.out.println("기초 데이터 생성 완료!");
    }

    private Department saveDept(String name, Department parent) {
        return departmentRepository.save(Department.builder()
                .deptName(name)
                .parent(parent)
                .build());
    }

    private void saveUser(String loginId, String pw, String name, Department dept, String position, UserRole role) {
        userRepository.save(User.builder()
                .loginId(loginId)
                .password(pw)
                .name(name) // 🌟 여기서 자동으로 암호화되어 저장됨!
                .email(loginId + "@test.com")
                .phone("010-0000-0000")
                .address("서울시 강남구")
                .department(dept)
                .position(position)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build());
    }
}