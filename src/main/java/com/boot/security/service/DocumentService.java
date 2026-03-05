package com.boot.security.service;

import com.boot.security.dto.ApprovalRequest;
import com.boot.security.dto.DocumentCreateRequest;
import com.boot.security.entity.*;
import com.boot.security.enums.DocumentStatus;
import com.boot.security.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
//import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentFileRepository documentFileRepository;
    private final ApprovalRepository approvalRepository;
    // 🌟 추가된 참조자 Repository
    private final DocumentObserverRepository documentObserverRepository;

    public Optional<Document> findById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> findAll() {
        return documentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Transactional
    public Long createDocument(DocumentCreateRequest req, Long userId) {
        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다."));

        // 1. 문서 객체 생성 (🌟 observers 리스트 초기화 추가)
        Document doc = Document.builder()
                .writer(writer)
                .title(req.getTitle())
                .content(req.getContent())
                .type(req.getType())
                .priority(req.getPriority())
                .status(DocumentStatus.PENDING)
                .approvals(new ArrayList<>())
                .files(new ArrayList<>())
                .observers(new ArrayList<>()) // 🌟 여기서 리스트 초기화 해줍니다.
                .build();

        // 2. 기안자(0번) 칸 생성 (리스트에만 추가, save 호출 X)
        doc.getApprovals().add(Approval.builder()
                .document(doc)
                .approver(writer)
                .order(0)
                .status("APPROVED")
                .comment("")
                .approvedAt(LocalDateTime.now())
                .build());

        // 3. 결재자 칸 생성 (리스트에만 추가, save 호출 X)
        if (req.getApproverIds() != null) {
            int order = 1;
            for (Long approverId : req.getApproverIds()) {
                User approver = userRepository.findById(approverId).orElseThrow();
                doc.getApprovals().add(Approval.builder()
                        .document(doc).approver(approver).order(order++)
                        .status("PENDING").comment("").build());
            }
        }

        // 🌟 4. 참조자 저장 로직 추가 (결재 진행 전, 기안 단계에서 참조자 지정)
        if (req.getObserverIds() != null && !req.getObserverIds().isEmpty()) {
            for (Long observerId : req.getObserverIds()) {
                User observer = userRepository.findById(observerId)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                doc.getObservers().add(DocumentObserver.builder()
                        .document(doc)
                        .user(observer)
                        .build());
            }
        }

        // 5. 파일 처리 (handleFileUpload 내부에서 doc.getFiles().add() 수행)
        handleFileUpload(req.getFiles(), doc);

        // 6. 최종 한 번만 저장 (CascadeType.ALL에 의해 중복 없이 전체 저장됨)
        return documentRepository.save(doc).getId();
    }

    @Transactional
    public void approve(Long docId, Long userId, ApprovalRequest req) {
        // 1. 해당 문서와 사용자의 결재 정보를 찾습니다.
        Optional<Approval> approvalOpt = approvalRepository.findByDocumentIdAndApproverId(docId, userId);

        // 🌟 [수정] 의견 등록(COMMENT) 처리 로직
        if ("COMMENT".equals(req.getStatus())) {
            if (approvalOpt.isPresent()) {
                // 이미 레코드가 있으면 의견만 업데이트 (Dirty Checking 활용)
                approvalOpt.get().setComment(req.getComment());
            } else {
                // 만약 레코드가 없다면(기존 문서 등) 새로 생성해줍니다.
                Document doc = documentRepository.findById(docId)
                        .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다."));
                User user = userRepository.findById(userId).orElseThrow();

                Approval newComment = Approval.builder()
                        .document(doc)
                        .approver(user)
                        .order(0) // 기안자 의견으로 간주
                        .status("COMMENT")
                        .comment(req.getComment())
                        .approvedAt(LocalDateTime.now())
                        .build();
                approvalRepository.save(newComment);
            }
            return; // 의견 등록은 여기서 종료 (updateDocumentTotalStatus 호출 안 함)
        }

        // 2. 승인/반려 처리 (기존 로직 유지하되 안전하게 처리)
        Approval approval = approvalOpt.orElseThrow(() -> new RuntimeException("결재 권한이 없습니다."));
        approval.setComment(req.getComment());
        approval.setStatus(req.getStatus());
        approval.setApprovedAt(LocalDateTime.now());

        // 🌟 approvalRepository.save(approval)는 필요 없습니다. @Transactional이 자동 저장합니다.
        updateDocumentTotalStatus(docId, req.getStatus());
    }

    private void updateDocumentTotalStatus(Long docId, String currentStatus) {
        Document doc = documentRepository.findById(docId).orElseThrow();

        if ("REJECTED".equals(currentStatus)) {
            doc.setStatus(DocumentStatus.REJECTED);
            doc.setCompletedAt(LocalDateTime.now());
        } else {
            List<Approval> approvals = approvalRepository.findByDocumentId(docId);
            boolean isAllApproved = approvals.stream()
                    .filter(a -> a.getOrder() > 0)
                    .allMatch(a -> "APPROVED".equals(a.getStatus()));

            if (isAllApproved) {
                doc.setStatus(DocumentStatus.APPROVED);
                doc.setCompletedAt(LocalDateTime.now());
            }
        }
    }

    private void handleFileUpload(List<MultipartFile> files, Document doc) {
        if (files == null || files.isEmpty()) return;
        
        // 🌟 [핵심 수정] 윈도우/리눅스(서버) 어디서든 작동하도록 상대 경로로 변경
        String uploadDir = System.getProperty("user.dir") + "/uploads/documents/";
        File dir = new File(uploadDir);

        if (!dir.exists()) {
            boolean created = dir.mkdirs(); 
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String uuid = UUID.randomUUID().toString();
            String savedPath = uploadDir + uuid + "_" + file.getOriginalFilename();
            try {
                file.transferTo(new File(savedPath));
                // 리스트에만 추가 (save 호출 X)
                doc.getFiles().add(DocumentFile.builder()
                        .originalFileName(file.getOriginalFilename())
                        .savedFilePath(savedPath).fileSize(file.getSize()).document(doc).build());
            } catch (IOException e) {
                // 🌟 에러 메시지도 상세하게 찍어주도록 수정
                throw new RuntimeException("파일 저장 실패 (경로 확인 필요): " + e.getMessage());
            }
        }
    }



    @Transactional
    public void deleteDocument(Long id, User currentUser) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다."));

        boolean isWriter = doc.getWriter().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        boolean isHead = "본부장".equals(currentUser.getPosition());

        if ((isWriter && doc.getStatus() == DocumentStatus.PENDING) || isAdmin || isHead) {
            documentRepository.delete(doc);
        } else {
            throw new RuntimeException("삭제 권한이 없거나 삭제 가능한 상태가 아닙니다.");
        }
    }
    @Transactional
    public void updateObserverReadStatus(Long documentId, Long userId) { // String -> Long으로 변경
        // 1. 이미 컨트롤러에서 검증된 userId를 넘겨받으므로 User 조회 로직이 필요 없습니다.
        // 2. 바로 참조자 정보를 조회합니다.
        DocumentObserver observer = documentObserverRepository.findByDocumentIdAndUserId(documentId, userId)
                .orElseThrow(() -> new RuntimeException("해당 문서의 참조자가 아닙니다."));

        // 3. 읽음 처리
        if (!observer.isRead()) {
            observer.setRead(true);
            observer.setReadAt(LocalDateTime.now());
        }
    }
    @Transactional
    public void updateDocument(Long id, DocumentCreateRequest req, User currentUser) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문서를 찾을 수 없습니다."));

        // 1. 작성자 본인 확인
        if (!doc.getWriter().getId().equals(currentUser.getId())) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 2. 결재 진행 여부 확인 (매우 중요!)
        // 기안자(0번)를 제외한 나머지 결재자(order > 0) 중 한 명이라도 PENDING이 아니면 수정 불가
        boolean isProcessed = doc.getApprovals().stream()
                .filter(a -> a.getOrder() > 0)
                .anyMatch(a -> !"PENDING".equals(a.getStatus()));

        if (isProcessed || doc.getStatus() != DocumentStatus.PENDING) {
            throw new RuntimeException("이미 결재가 진행되었거나 완료된 문서는 내용 및 참조자를 수정할 수 없습니다.");
        }

        // 3. 필드 업데이트 (프론트에서 보낸 값만 업데이트하도록 방어 코드)
        if (req.getTitle() != null) doc.setTitle(req.getTitle());
        if (req.getContent() != null) doc.setContent(req.getContent());
        if (req.getType() != null) doc.setType(req.getType());
        if (req.getPriority() != null) doc.setPriority(req.getPriority());

        // 결재선 수정 로직 (기존 유지)
        if (req.getApproverIds() != null && !req.getApproverIds().isEmpty()) {
            approvalRepository.deleteByDocumentIdAndOrderGreaterThan(id, 0);
            doc.getApprovals().removeIf(a -> a.getOrder() > 0);

            int order = 1;
            for (Long approverId : req.getApproverIds()) {
                User approver = userRepository.findById(approverId).orElseThrow();
                doc.getApprovals().add(Approval.builder()
                        .document(doc).approver(approver).order(order++)
                        .status("PENDING").comment("").build());
            }
        }

        // 🌟 4. 참조자 수정 로직 추가 (기존꺼 지우고 새로 담기)
        if (req.getObserverIds() != null) {
            // DB에 저장된 기존 참조자 데이터를 비웁니다.
            documentObserverRepository.deleteByDocumentId(id);
            // JPA 영속성 컨텍스트(List)도 비워줍니다.
            if (doc.getObservers() != null) {
                doc.getObservers().clear();
            } else {
                doc.setObservers(new ArrayList<>());
            }

            // 새로운 참조자 리스트로 채워 넣습니다.
            for (Long observerId : req.getObserverIds()) {
                User observer = userRepository.findById(observerId)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

                doc.getObservers().add(DocumentObserver.builder()
                        .document(doc)
                        .user(observer)
                        .build());
            }
        }

        handleFileUpload(req.getFiles(), doc);
    }
}
