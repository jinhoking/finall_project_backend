package com.boot.security.repository;

import com.boot.security.entity.NoticeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {
    List<NoticeComment> findByNoticeIdOrderByCreatedAtAsc(Long noticeId);
}