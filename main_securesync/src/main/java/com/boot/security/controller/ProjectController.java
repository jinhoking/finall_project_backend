package com.boot.security.controller;

import com.boot.security.annotation.AuditLog;
import com.boot.security.dto.ProjectResponse;
import com.boot.security.entity.Project;
import com.boot.security.entity.ProjectDetail;
//import com.boot.security.entity.ProjectIssue;
import com.boot.security.entity.User;
//import com.boot.security.entity.Department;
//import com.boot.security.repository.DepartmentRepository;
import com.boot.security.repository.ProjectRepository;
import com.boot.security.repository.UserRepository;
import com.boot.security.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;


    // 프로젝트 목록 조회
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // 1. 신규 프로젝트 등록
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN','MANAGER')")
    @AuditLog(action = "신규 프로젝트 등록", type = "system")
    public ResponseEntity<?> createProject(@RequestBody Map<String, Object> request) {
        try {
            String pmName = (String) request.get("managerName");

            // 🌟 2번 방식: 이름으로 유저를 찾고 "개발팀" 소속인지 검증
            User manager = userRepository.findByName(pmName)
                    .filter(u -> u.getDepartment() != null && "개발팀".equals(u.getDepartment().getDeptName()))
                    .orElse(null);

            Project project = Project.builder()
                    .projectName((String) request.get("projectName"))
                    .description((String) request.get("description"))
                    .manager(manager)      // 실제 유저 엔티티 연결
                    .managerName(pmName)   // 입력한 이름 백업
                    .startDate(LocalDateTime.parse(request.get("startDate") + "T00:00:00"))
                    .feDevs((String) request.get("fe_devs"))
                    .beDevs((String) request.get("be_devs"))
                    .feTechStack((String) request.get("fe_tech"))
                    .beTechStack((String) request.get("be_tech"))
                    .totalProgress(0)
                    .status("기획/착수")
                    .currentStep(0)
                    .build();

            ProjectDetail detail = ProjectDetail.builder()
                    .project(project)
                    .backendTotal(0).frontendTotal(0).javaProgress(0).reactProgress(0).dbProgress(0).uiCssProgress(0)
                    .build();

            project.setDetail(detail);
            Project savedProject = projectRepository.save(project);

            return ResponseEntity.ok(Map.of("id", savedProject.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("등록 실패: " + e.getMessage());
        }
    }

    // 2. 프로젝트 상태 업데이트 (진행률 슬라이더 수정 시 호출)
    @PutMapping("/{id}")
    @AuditLog(action = "프로젝트 진척도 업데이트", type = "system")
    public ResponseEntity<?> updateProjectStatus(@PathVariable Long id, @RequestBody ProjectResponse updateDto) {
        try {
            projectService.updateProjectProgress(id, updateDto);
            return ResponseEntity.ok("업데이트 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("업데이트 실패: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN' ,'MANAGER')") // 🌟 삭제는 관리자만 가능하게 설정 (보안상 안전)
    @AuditLog(action = "프로젝트 삭제", type = "danger")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.ok("프로젝트가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    // 3. 새로운 이슈 등록 (빨간 경고 로그 발사)
    @PostMapping("/{id}/issues")
    @AuditLog(action = "프로젝트 이슈 발생 보고", type = "warning")
    public ResponseEntity<?> addProjectIssue(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            projectService.addIssue(id, request);
            return ResponseEntity.ok("이슈 등록 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이슈 등록 실패: " + e.getMessage());
        }
    }
}