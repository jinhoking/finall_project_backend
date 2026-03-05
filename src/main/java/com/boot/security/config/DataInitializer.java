package com.boot.security.config;

import com.boot.security.entity.Department;
import com.boot.security.entity.User;
import com.boot.security.enums.UserStatus;
import com.boot.security.repository.DepartmentRepository;
import com.boot.security.repository.UserRepository;
import com.boot.security.role.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
//import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // 🌟 ID를 강제로 꽂기 위해 추가
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional // 🌟 Native Query를 쓰기 때문에 꼭 붙여주세요!
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            System.out.println("이미 데이터가 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("🚀 기초 데이터 생성 시작 (ID 고정 모드)...");

        // 2. 부서 생성 (원하는 ID를 첫 번째 파라미터로 넣어주세요!)
        Department main = saveDept(1L, "본사", null);
        Department strategyDiv = saveDept(2L, "전략기획본부", main);
        Department devDiv = saveDept(3L, "개발지원본부", main);

        Department hrTeam = saveDept(10L, "인사팀", strategyDiv);    // 10번
        Department mgtTeam = saveDept(11L, "경영지원팀", strategyDiv); // 11번
        Department devTeam = saveDept(12L, "개발팀", devDiv);      // 🌟 개발팀은 무조건 12번!
        Department secTeam = saveDept(13L, "보안팀", devDiv);      // 13번

        // 3. 사용자 생성 (찌노님 코드 그대로 유지!)
        String pw = passwordEncoder.encode("1234");

        saveUser("admin", pw, "관리자", null, "관리자", UserRole.ROLE_ADMIN);
        saveUser("head_strategy", pw, "전략본부장", strategyDiv, "본부장", UserRole.ROLE_MANAGER);
        saveUser("head_dev", pw, "개발본부장", devDiv, "본부장", UserRole.ROLE_MANAGER);

        saveUser("hr_leader", pw, "인사팀장", hrTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("hr_cha1", pw, "인사차장", hrTeam, "차장", UserRole.ROLE_USER);
        saveUser("hr_kwa1", pw, "인사과장", hrTeam, "과장", UserRole.ROLE_USER);
        saveUser("hr_daeri1", pw, "인사대리", hrTeam, "대리", UserRole.ROLE_USER);
        saveUser("hr_sawon1", pw, "인사사원", hrTeam, "사원", UserRole.ROLE_USER);

        saveUser("mgt_leader", pw, "경영팀장", mgtTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("mgt_sawon1", pw, "경영사원", mgtTeam, "사원", UserRole.ROLE_USER);

        saveUser("dev_leader", pw, "개발팀장", devTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("dev_cha1", pw, "개발차장", devTeam, "차장", UserRole.ROLE_USER);
        saveUser("dev_kwa1", pw, "개발과장", devTeam, "과장", UserRole.ROLE_USER);
        saveUser("dev_daeri1", pw, "개발대리", devTeam, "대리", UserRole.ROLE_USER);
        saveUser("dev_sawon1", pw, "개발사원", devTeam, "사원", UserRole.ROLE_USER);

        saveUser("sec_leader", pw, "보안팀장", secTeam, "팀장", UserRole.ROLE_MANAGER);
        saveUser("sec_sawon1", pw, "보안사원", secTeam, "사원", UserRole.ROLE_USER);

        System.out.println("✅ 모든 데이터가 정해진 ID로 생성되었습니다!");
    }

    // 🌟 [수정된 saveDept] ID를 강제로 INSERT 하는 마법의 구문
    private Department saveDept(Long id, String name, Department parent) {
        String sql = "INSERT INTO departments (id, dept_name, parent_id) VALUES (?, ?, ?)";
        entityManager.createNativeQuery(sql)
                .setParameter(1, id)
                .setParameter(2, name)
                .setParameter(3, parent != null ? parent.getId() : null)
                .executeUpdate();

        // 저장된 객체를 다시 조회해서 리턴 (그래야 saveUser에서 쓸 수 있음)
        return departmentRepository.findById(id).orElseThrow();
    }

    private void saveUser(String loginId, String pw, String name, Department dept, String position, UserRole role) {
        userRepository.save(User.builder()
                .loginId(loginId)
                .password(pw)
                .name(name)
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