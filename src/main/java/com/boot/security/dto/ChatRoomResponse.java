// com.boot.security.dto.ChatRoomResponse.java
package com.boot.security.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomResponse {
    private String roomId;
    private Long partnerId;
    private String partnerName;
    private String partnerPos;
    private String partnerDept;
    private String lastMessage;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastTime; // 🌟 프론트에서 item.lastTime으로 사용함
    private long unreadCount;
}