package com.boot.security.repository;

import com.boot.security.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 카테고리별 검색 (전체/공지/중요/이벤트)
    List<Notice> findByCategoryOrderByCreatedAtDesc(String category);

    // 제목 또는 내용으로 검색
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% OR n.content LIKE %:keyword% ORDER BY n.createdAt DESC")
    List<Notice> searchByKeyword(@Param("keyword") String keyword);
}