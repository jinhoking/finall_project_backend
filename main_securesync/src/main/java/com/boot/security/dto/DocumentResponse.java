package com.boot.security.dto;

import com.boot.security.entity.Document;
import com.boot.security.entity.DocumentFile;
import com.boot.security.entity.DocumentObserver;
import com.boot.security.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class DocumentResponse {
    private Long id;
    private Long writerId;
    private String type;
    private String title;
    private String content;
    private String drafterName;
    private String deptName;
    private String position;
    private String status;
    private String priority;
    private LocalDateTime createdAt;

    private Long currentApproverId;

    private List<ApprovalResponse> approvals = new ArrayList<>();
    private List<ObserverDto> observers = new ArrayList<>();
    private List<FileResponse> files = new ArrayList<>();

    public DocumentResponse(Document doc) {
        this.id = doc.getId();
        this.writerId = doc.getWriter().getId();
        this.type = doc.getType();
        this.title = doc.getTitle();
        this.content = doc.getContent();
        this.drafterName = doc.getWriter().getName();
        this.position = doc.getWriter().getPosition();

        if (doc.getWriter().getDepartment() != null) {
            this.deptName = doc.getWriter().getDepartment().getDeptName();
        }

        this.priority = doc.getPriority();
        this.createdAt = doc.getCreatedAt();

        if (doc.getStatus() != null) {
            this.status = doc.getStatus().name();
        }

        if (doc.getApprovals() != null) {
            this.approvals = doc.getApprovals().stream()
                    .map(ApprovalResponse::new)
                    .collect(Collectors.toList());

            doc.getApprovals().stream()
                    .filter(app -> app.getStatus() != null)
                    .filter(app -> "PENDING".equals(String.valueOf(app.getStatus())))
                    .findFirst()
                    .ifPresent(currentStep -> {
                        if (currentStep.getApprover() != null) {
                            this.currentApproverId = currentStep.getApprover().getId();
                        }
                    });
        }

        if (doc.getFiles() != null) {
            this.files = doc.getFiles().stream()
                    .map(FileResponse::new)
                    .collect(Collectors.toList());
        }

        // 🌟 [핵심 수정] obs.getUser()가 아니라 DocumentObserver 객체 자체를 넘겨야 합니다.
        if (doc.getObservers() != null) {
            this.observers = doc.getObservers().stream()
                    .map(ObserverDto::new) // 🌟 수정됨: new ObserverDto(DocumentObserver) 생성자 호출
                    .collect(Collectors.toList());
        }
    }

    @Getter
    public static class FileResponse {
        private Long id;
        private String oriName;

        public FileResponse(DocumentFile file) {
            this.id = file.getId();
            this.oriName = file.getOriginalFileName();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObserverDto {
        private Long id;
        private String name;
        private String deptName;
        private String position;
        private boolean isRead;
        private LocalDateTime readAt;

        // 🌟 프론트엔드로 보낼 때 DB의 실시간 상태를 담는 생성자
        public ObserverDto(DocumentObserver observer) {
            this.id = observer.getUser().getId();
            this.name = observer.getUser().getName();
            this.position = observer.getUser().getPosition();
            this.deptName = (observer.getUser().getDepartment() != null)
                    ? observer.getUser().getDepartment().getDeptName()
                    : "미소속";
            this.isRead = observer.isRead();   // 🔥 DB의 실제 읽음 상태 매핑
            this.readAt = observer.getReadAt(); // 🔥 DB의 읽은 시간 매핑
        }
    }
}