package com.boot.security.repository;

import com.boot.security.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // 1. 🌟 대시보드 리스트를 가져올 때 상세 정보(Detail)까지 한 번에 쿼리로 가져오기 (성능 최적화)
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.detail")
    List<Project> findAllWithDetail();

    // 2. 🌟 특정 프로젝트를 볼 때 이슈(Issues)와 상세정보(Detail)를 한 번에 가져오기
    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.detail " +
            "LEFT JOIN FETCH p.issues " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithDetailAndIssues(@Param("id") Long id);

    // 3. 프로젝트 이름으로 검색 (필요 시)
    List<Project> findByProjectNameContainingIgnoreCase(String projectName);
}