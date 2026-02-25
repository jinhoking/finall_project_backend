package com.boot.security.repository;

import com.boot.security.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    List<Approval> findByDocumentId(Long documentId);
    Optional<Approval> findByDocumentIdAndApproverId(Long documentId, Long approverId);
    void deleteByDocumentId(Long documentId);

    // 🌟 수정 시 기안자(0번)는 남기고 결재자(1번 이상)만 지우기 위해 필요합니다.
    void deleteByDocumentIdAndOrderGreaterThan(Long documentId, Integer order);
}