package com.boot.security.repository;

import com.boot.security.entity.DocumentFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
}