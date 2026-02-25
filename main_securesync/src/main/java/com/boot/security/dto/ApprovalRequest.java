package com.boot.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ApprovalRequest {
    private String status;  // APPROVED, REJECTED, COMMENT
    private String comment; // 결재 의견
}