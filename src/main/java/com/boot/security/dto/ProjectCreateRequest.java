package com.boot.security.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProjectCreateRequest {
    private String projectName;
    private String managerName; // 프론트에서 보내는 PM 이름
    private String startDate;   // "2024-03-20" 형태
    private String description;
    private List<String> fe_techs;
    private List<String> be_techs;
}