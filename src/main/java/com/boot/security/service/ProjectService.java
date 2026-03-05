package com.boot.security.service;

import com.boot.security.dto.ProjectResponse;
import com.boot.security.entity.Project;
import com.boot.security.entity.ProjectDetail;
import com.boot.security.entity.ProjectIssue;
import com.boot.security.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ProjectResponse convertToDto(Project p) {
        ProjectDetail d = p.getDetail();
        int feCount = (p.getFeDevs() != null && !p.getFeDevs().isEmpty()) ? p.getFeDevs().split(",").length : 0;
        int beCount = (p.getBeDevs() != null && !p.getBeDevs().isEmpty()) ? p.getBeDevs().split(",").length : 0;

        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getProjectName())
                .manager(p.getManager() != null ? p.getManager().getName() : p.getManagerName())
                .description(p.getDescription())
                .progress(p.getTotalProgress())
                .status(p.getStatus())
                .currentStep(p.getCurrentStep())
                .startDate(p.getStartDate() != null ? p.getStartDate().toString().substring(0, 10) : "")
                .devCount(feCount + beCount)
                .fe_progress(d != null ? d.getFrontendTotal() : 0)
                .be_progress(d != null ? d.getBackendTotal() : 0)
                .devs(new ProjectResponse.Devs(p.getFeDevs(), p.getBeDevs()))
                .techStack(mapTechStack(p)) // 🌟 DB 수치를 리액트 규격으로 변환
                .issues(p.getIssues().stream()
                        .map(i -> new ProjectResponse.IssueDto(i.getId(), i.getTitle(), i.getType(), i.getWriter(), i.getCreatedAt().toString().substring(0, 10)))
                        .collect(Collectors.toList()))
                .build();
    }

    private ProjectResponse.TechStack mapTechStack(Project p) {
        List<ProjectResponse.TechDetail> fe = new ArrayList<>();
        List<ProjectResponse.TechDetail> be = new ArrayList<>();
        ProjectDetail d = p.getDetail();

        if (p.getFeTechStack() != null && !p.getFeTechStack().isEmpty()) {
            Arrays.stream(p.getFeTechStack().split(",")).forEach(s -> {
                String name = s.trim();
                int progress = 0;
                if (d != null) {
                    String low = name.toLowerCase();
                    // 🌟 정규식 확장: React 그룹
                    if (low.matches(".*(react|redux|next|vue|angular).*")) progress = d.getReactProgress();
                        // 🌟 정규식 확장: UI/Vite 그룹
                    else if (low.matches(".*(typescript|javascript|ts|js|tailwind|bootstrap|css|vite).*")) progress = d.getUiCssProgress();
                }
                fe.add(new ProjectResponse.TechDetail(name, progress));
            });
        }
        if (p.getBeTechStack() != null && !p.getBeTechStack().isEmpty()) {
            Arrays.stream(p.getBeTechStack().split(",")).forEach(s -> {
                String name = s.trim();
                int progress = 0;
                if (d != null) {
                    String low = name.toLowerCase();
                    // 🌟 정규식 확장: Java/Node 그룹
                    if (low.matches(".*(java|spring|node|python|django).*")) progress = d.getJavaProgress();
                        // 🌟 정규식 확장: DB/Infra 그룹
                    else if (low.matches(".*(mysql|maria|db|postgres|oracle|mongo|redis|docker|kuber|nginx|firebase|linux|git).*")) progress = d.getDbProgress();
                }
                be.add(new ProjectResponse.TechDetail(name, progress));
            });
        }
        return new ProjectResponse.TechStack(fe, be);
    }

    @Transactional
    public void updateProjectProgress(Long id, ProjectResponse updateDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 프로젝트를 찾을 수 없습니다. ID: " + id));

        project.setCurrentStep(updateDto.getCurrentStep());
        project.setStatus(updateDto.getStatus());
        project.setTotalProgress(updateDto.getProgress());

        ProjectDetail tempDetail = project.getDetail();
        if (tempDetail == null) {
            tempDetail = ProjectDetail.builder().project(project).build();
            project.setDetail(tempDetail);
        }

        // 🌟 람다 에러 해결을 위한 Effectively Final 변수
        final ProjectDetail finalDetail = tempDetail;

        finalDetail.setFrontendTotal(updateDto.getFe_progress());
        finalDetail.setBackendTotal(updateDto.getBe_progress());

        if (updateDto.getTechStack() != null) {
            // Frontend 저장 매핑 확장
            if (updateDto.getTechStack().getFe() != null) {
                updateDto.getTechStack().getFe().forEach(tech -> {
                    String low = tech.getName().toLowerCase();
                    if (low.matches(".*(react|redux|next|vue|angular).*"))
                        finalDetail.setReactProgress(tech.getProgress());
                    else if (low.matches(".*(typescript|javascript|ts|js|tailwind|bootstrap|css|vite).*"))
                        finalDetail.setUiCssProgress(tech.getProgress());
                });
            }

            // Backend 저장 매핑 확장
            if (updateDto.getTechStack().getBe() != null) {
                updateDto.getTechStack().getBe().forEach(tech -> {
                    String low = tech.getName().toLowerCase();
                    if (low.matches(".*(java|spring|node|python|django).*"))
                        finalDetail.setJavaProgress(tech.getProgress());
                    else if (low.matches(".*(mysql|maria|db|postgres|oracle|mongo|redis|docker|kuber|nginx|firebase|linux|git).*"))
                        finalDetail.setDbProgress(tech.getProgress());
                });
            }
        }
    }

    @Transactional
    public void addIssue(Long projectId, Map<String, Object> request) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        ProjectIssue issue = ProjectIssue.builder()
                .title((String) request.get("title"))
                .writer((String) request.get("writer"))
                .type((String) request.get("type"))
                .createdAt(LocalDateTime.now())
                .build();
        project.getIssues().add(issue);

    }
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("삭제할 프로젝트가 없습니다."));
        projectRepository.delete(project);
    }
}