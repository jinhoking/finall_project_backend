// com.boot.security.service.ChatService.java 수정
package com.boot.security.service;

import com.boot.security.dto.ChatRoomResponse;
import com.boot.security.entity.ChatEntity;
import com.boot.security.entity.User;
import com.boot.security.repository.ChatRepository;
import com.boot.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public List<ChatRoomResponse> getChatRoomList(Long userId) {
        List<ChatEntity> entities = chatRepository.findRecentMessagesByUserId(userId);

        return entities.stream()
                .map(entity -> {
                    Long senderId = entity.getSenderId();
                    Long receiverId = entity.getReceiverId();

                    if (senderId == null || receiverId == null) return null;

                    Long partnerId = senderId.equals(userId) ? receiverId : senderId;
                    if (partnerId == null) return null;

                    User partner = userRepository.findById(partnerId).orElse(null);

                    // 🌟 [핵심 수정] 부서명 추출 로직 추가
                    String deptName = "소속 없음";
                    if (partner != null && partner.getDepartment() != null) {
                        deptName = partner.getDepartment().getDeptName();
                    }

                    return ChatRoomResponse.builder()
                            .roomId(entity.getRoomId())
                            .partnerId(partnerId)
                            .partnerName(partner != null ? partner.getName() : "탈퇴한 사용자")
                            .partnerPos(partner != null ? partner.getPosition() : "")
                            .partnerDept(deptName) // 🌟 이제 실제 부서명이 전달됩니다.
                            .lastMessage(entity.getMessage())
                            .lastTime(entity.getTimestamp())
                            .unreadCount(0) // 실제 안읽은 개수 로직이 있다면 여기에 추가
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void markAsRead(String roomId, Long userId) {
        List<ChatEntity> unread = chatRepository.findByRoomIdAndReceiverIdAndIsReadFalse(roomId, userId);
        unread.forEach(m -> m.setRead(true));
        chatRepository.saveAll(unread);
    }
}