package com.boot.security.dto;

import com.boot.security.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class NoticeResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private String writer;
    private String deptName;
    private String position;
    private String date;
    private int views;
    private int commentCount;
    private boolean hasFile;
    private String coverImageUrl;
    private List<CommentDto> comments;
    private List<FileDto> files; // 🌟 최상위 리스트로 이동

    @Getter @Builder
    public static class FileDto {
        private Long id;
        private String originalName;
        private long size;
        private String downloadUrl;
    }

    @Getter @Builder
    public static class CommentDto {
        private Long id;
        private String dept;
        private String rank;
        private String user;
        private String text;
        private String date;
    }

    public static NoticeResponse fromEntity(Notice notice, String coverUrl, boolean hasFile) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 작성일 Null 방어
        String formattedDate = (notice.getCreatedAt() != null)
                ? notice.getCreatedAt().format(formatter)
                : LocalDateTime.now().format(formatter);

        String dept = (notice.getWriter().getDepartment() != null)
                ? notice.getWriter().getDepartment().getDeptName() : "소속없음";

        // 🌟 첨부파일 리스트 생성 (isCover가 false인 것들만)
        List<FileDto> fileList = (notice.getFiles() != null) ? notice.getFiles().stream()
                .filter(f -> !f.isCover())
                .map(f -> FileDto.builder()
                        .id(f.getId())
                        .originalName(f.getOriginalFileName())
                        .size(f.getFileSize())
                        .downloadUrl("/api/notices/files/download/" + f.getId())
                        .build())
                .collect(Collectors.toList()) : Collections.emptyList();

        return NoticeResponse.builder()
                .id(notice.getId())
                .type(notice.getCategory())
                .title(notice.getTitle())
                .content(notice.getContent())
                .writer(notice.getWriter().getName())
                .deptName(dept)
                .position(notice.getWriter().getPosition())
                .date(formattedDate)
                .views(notice.getViewCount())
                .coverImageUrl(coverUrl)
                .hasFile(hasFile)
                .files(fileList) // 🌟 여기서 공지사항 객체에 직접 파일을 넣어줍니다.
                .comments(notice.getComments() != null ? notice.getComments().stream().map(c -> {
                    String cDate = (c.getCreatedAt() != null) ? c.getCreatedAt().format(formatter) : "방금 전";
                    String cDept = (c.getWriter().getDepartment() != null) ? c.getWriter().getDepartment().getDeptName() : "소속없음";
                    return CommentDto.builder()
                            .id(c.getId())
                            .dept(cDept)
                            .rank(c.getWriter().getPosition())
                            .user(c.getWriter().getName())
                            .text(c.getContent())
                            .date(cDate)
                            .build();
                }).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
}