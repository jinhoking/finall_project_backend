package com.boot.security.repository;

import com.boot.security.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // 내가 작성한 문서 찾기 (내림차순 정렬)
    List<Document> findByWriterIdOrderByCreatedAtDesc(Long writerId);
}