package com.boot.security.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityResponse {
    private List<AuditLogDto> logs;
    private int dmlCount;
    private int abnormalCount;
}