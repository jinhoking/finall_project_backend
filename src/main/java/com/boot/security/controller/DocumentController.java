package com.boot.security.controller;

import com.boot.security.annotation.AuditLog;
import com.boot.security.dto.ApprovalRequest;
import com.boot.security.dto.DocumentCreateRequest;
import com.boot.security.dto.DocumentResponse;
import com.boot.security.entity.Document;
import com.boot.security.entity.User;
import com.boot.security.service.DocumentService;
import com.boot.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    /**
     * 신규 기안서 작성
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createDocument(
            @ModelAttribute DocumentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body("로그인 정보가 없습니다.");
        }

        String loginId = userDetails.getUsername();
        User loginUser = userService.findByLoginId(loginId);
        Long userId = loginUser.getId();

        Long docId = documentService.createDocument(request, userId);
        return ResponseEntity.ok(docId);
    }

    /**
     * 전체 문서 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<Document> documents = documentService.findAll();
        List<DocumentResponse> response = documents.stream()
                .map(DocumentResponse::new)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 문서 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        try {
            Document doc = documentService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));
            return ResponseEntity.ok(new DocumentResponse(doc));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("문서를 찾을 수 없습니다.");
        }
    }

    /**
     * 통합 결재 처리 (승인 / 반려 / 의견등록)
     * 프론트엔드에서 { status: 'APPROVED' | 'REJECTED' | 'COMMENT', comment: '...' } 형태로 요청
     */
    @AuditLog(action = "전자결재 승인/반려 처리")
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> processApproval(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) return ResponseEntity.status(401).body("인증 정보가 없습니다.");

            User currentUser = userService.findByLoginId(userDetails.getUsername());

            // 🌟 Service에서 status(APPROVED, REJECTED, COMMENT)에 따라 분기 처리됨
            documentService.approve(id, currentUser.getId(), request);

            String msg = "COMMENT".equals(request.getStatus()) ? "의견이 등록되었습니다." : "결재 처리가 완료되었습니다.";
            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("처리 중 오류: " + e.getMessage());
        }
    }
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. 이미 컨트롤러에 있는 userService를 사용해 로그인 ID로 실제 User 객체를 찾습니다.
        User loginUser = userService.findByLoginId(userDetails.getUsername());

        // 2. 서비스에는 유저의 '이메일'이나 '이름'이 아닌 고유 PK인 'ID(Long)'를 전달합니다.
        documentService.updateObserverReadStatus(id, loginUser.getId());

        return ResponseEntity.ok().build();
    }

    @AuditLog(action = "전자결재 문서 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) return ResponseEntity.status(401).body("인증 정보가 없습니다.");

            User currentUser = userService.findByLoginId(userDetails.getUsername());

            // Service의 deleteDocument 호출
            documentService.deleteDocument(id, currentUser);

            return ResponseEntity.ok("문서가 삭제되었습니다.");
        } catch (RuntimeException e) {
            // 권한 없음 등의 예외 처리
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @ModelAttribute DocumentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) return ResponseEntity.status(401).body("인증 정보가 없습니다.");

            User currentUser = userService.findByLoginId(userDetails.getUsername());

            // Service의 updateDocument 호출
            documentService.updateDocument(id, request, currentUser);

            return ResponseEntity.ok("문서가 성공적으로 수정되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("수정 처리 중 오류가 발생했습니다.");
        }
    }
}