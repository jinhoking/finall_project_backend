package com.boot.security.controller;

import com.boot.security.dto.ChatMessage;
//import com.boot.security.dto.ChatRoomResponse;
import com.boot.security.entity.ChatEntity;
import com.boot.security.entity.User;
import com.boot.security.repository.UserRepository;
import com.boot.security.service.ChatService;
import com.boot.security.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final UserRepository userRepository; // 🌟 유저 조회를 위해 추가

    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        ChatEntity chatEntity = ChatEntity.builder()
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .receiverId(message.getReceiverId())
                .message(message.getMessage())
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        chatRepository.save(chatEntity);
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), chatEntity);
    }

    @GetMapping("/api/chat/history/{roomId}")
    public List<ChatEntity> getChatHistory(@PathVariable String roomId) {
        return chatRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    @GetMapping("/api/chat/rooms")
    public ResponseEntity<?> getChatRooms(Principal principal) {
        User user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🌟 가공된 DTO 리스트를 반환합니다.
        return ResponseEntity.ok(chatService.getChatRoomList(user.getId()));
    }
    @GetMapping("/api/chat/unread-count")
    public ResponseEntity<?> getTotalUnreadCount(Principal principal) {
        // 🌟 문자열 "hr_leader"를 숫자로 바꾸려던 코드를 수정합니다.
        User user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return ResponseEntity.ok(chatRepository.countByReceiverIdAndIsReadFalse(user.getId()));
    }

    @PutMapping("/api/chat/read/{roomId}")
    public ResponseEntity<?> markAsRead(@PathVariable String roomId, Principal principal) {
        User user = userRepository.findByLoginId(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        chatService.markAsRead(roomId, user.getId());
        return ResponseEntity.ok().build();
    }
}