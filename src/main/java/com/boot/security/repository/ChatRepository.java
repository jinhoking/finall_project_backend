package com.boot.security.repository;

import com.boot.security.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findByRoomIdOrderByTimestampAsc(String roomId);

    // 🌟 내가 수신자인데 안 읽은 메시지 개수
    long countByReceiverIdAndIsReadFalse(Long userId);

    // 🌟 특정 방에서 내가 수신자인 안 읽은 메시지들 찾기
    List<ChatEntity> findByRoomIdAndReceiverIdAndIsReadFalse(String roomId, Long userId);

    // 🌟 채팅방 목록: 내가 참여한 방의 마지막 메시지들 가져오기
    @Query("SELECT m FROM ChatEntity m WHERE m.id IN " +
            "(SELECT MAX(m2.id) FROM ChatEntity m2 WHERE m2.senderId = :userId OR m2.receiverId = :userId GROUP BY m2.roomId) " +
            "ORDER BY m.timestamp DESC")
    List<ChatEntity> findRecentMessagesByUserId(@Param("userId") Long userId);
}