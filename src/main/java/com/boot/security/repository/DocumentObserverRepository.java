package com.boot.security.repository;

import com.boot.security.entity.DocumentObserver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentObserverRepository extends JpaRepository<DocumentObserver, Long> {
    void deleteByDocumentId(Long documentId);
    Optional<DocumentObserver> findByDocumentIdAndUserId(Long documentId, Long userId);
}