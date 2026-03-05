package com.boot.security.dto;

import com.boot.security.entity.Approval;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ApprovalResponse {
    private Long id;
    private Long approverId;
    private String approverName;
    private String position;
    private String status;
    private String comment; // 🚩 이게 있어야 프론트 히스토리에 보임
    private LocalDateTime approvedAt;

    public ApprovalResponse(Approval approval) {
        this.id = approval.getId();
        this.approverName = approval.getApprover().getName();
        this.approverId = approval.getApprover().getId(); // 🚩 추가 확인
        this.position = approval.getApprover().getPosition();
        // 🚩 approval.getStatus()가 String이므로 .name() 없이 대입
        this.status = approval.getStatus();
        this.comment = approval.getComment();
        this.approvedAt = approval.getApprovedAt();
    }
}