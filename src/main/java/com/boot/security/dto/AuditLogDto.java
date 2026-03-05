package com.boot.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogDto {
    private Long id;
    private String time;
    private String msg;
    private String type;
}