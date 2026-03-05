package com.boot.security.controller;

import com.boot.security.dto.NoticeCommentRequest;
import com.boot.security.dto.NoticeRequest;
import com.boot.security.dto.NoticeResponse;
import com.boot.security.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeApiController {

    private final NoticeService noticeService;

    // 1. 공지사항 목록 조회 (필터/검색)
    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getNotices(
            @RequestParam(required = false, defaultValue = "전체") String category,
            @RequestParam(required = false) String keyword) {

        List<NoticeResponse> responses = noticeService.getNotices(category, keyword);
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/files/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(@PathVariable Long fileId) {
        try {
            com.boot.security.entity.NoticeFile noticeFile = noticeService.getNoticeFile(fileId);
            java.nio.file.Path path = java.nio.file.Paths.get(noticeFile.getFilePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                // 이미지 파일 타입 결정 (jpg, png 등)
                String contentType = java.nio.file.Files.probeContentType(path);
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long fileId) throws IOException {
        com.boot.security.entity.NoticeFile noticeFile = noticeService.getNoticeFile(fileId);
        java.nio.file.Path path = java.nio.file.Paths.get(noticeFile.getFilePath());
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

        // 파일명을 브라우저가 인식할 수 있게 인코딩
        String encodedFileName = java.net.URLEncoder.encode(noticeFile.getOriginalFileName(), "UTF-8").replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }

    // 2. 공지사항 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNoticeDetail(@PathVariable Long id) {
        NoticeResponse response = noticeService.getNoticeDetail(id);
        return ResponseEntity.ok(response);
    }

    // 3. 공지사항 작성 (Form-Data 처리)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponse> createNotice(
            @ModelAttribute NoticeRequest request,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage, // 🌟 RequestParam으로 변경
            @RequestParam(value = "files", required = false) List<MultipartFile> files,     // 🌟 RequestParam으로 변경
            Authentication authentication) throws IOException {

        String loginId = authentication.getName();
        NoticeResponse response = noticeService.createNotice(request, coverImage, files, loginId);
        return ResponseEntity.ok(response);
    }

    // 4. 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id, Authentication authentication) {
        String loginId = authentication.getName();
        noticeService.deleteNotice(id, loginId);
        return ResponseEntity.ok().build();
    }

    // 5. 댓글 작성
    @PostMapping("/{id}/comments")
    public ResponseEntity<NoticeResponse.CommentDto> addComment(
            @PathVariable Long id,
            @RequestBody NoticeCommentRequest request,
            Authentication authentication) {

        String loginId = authentication.getName();
        NoticeResponse.CommentDto response = noticeService.addComment(id, request, loginId);
        return ResponseEntity.ok(response);
    }
    // 6. 공지사항 수정
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoticeResponse> updateNotice(
            @PathVariable Long id,
            @ModelAttribute NoticeRequest request,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication) throws IOException {

        String loginId = authentication.getName();
        NoticeResponse response = noticeService.updateNotice(id, request, coverImage, files, loginId);
        return ResponseEntity.ok(response);
    }
}