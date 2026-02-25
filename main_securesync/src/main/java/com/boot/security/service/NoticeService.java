package com.boot.security.service;

import com.boot.security.dto.NoticeCommentRequest;
import com.boot.security.dto.NoticeRequest;
import com.boot.security.dto.NoticeResponse;
import com.boot.security.entity.Notice;
import com.boot.security.entity.NoticeComment;
import com.boot.security.entity.NoticeFile;
import com.boot.security.entity.User;
import com.boot.security.repository.NoticeCommentRepository;
import com.boot.security.repository.NoticeFileRepository;
import com.boot.security.repository.NoticeRepository;
import com.boot.security.repository.UserRepository;
import com.boot.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final NoticeCommentRepository noticeCommentRepository;
    private final UserRepository userRepository;

    @Value("${file.notice-upload-dir}")
    private String uploadDir;

    public NoticeFile getNoticeFile(Long fileId) {
        return noticeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));
    }

    // 1. 게시글 생성 (saveAndFlush로 500에러 방지)
    public NoticeResponse createNotice(NoticeRequest request, MultipartFile coverImage, List<MultipartFile> files, String loginId) throws IOException {
        User writer = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .writer(writer)
                .build();

        // 🌟 saveAndFlush를 사용해야 createdAt 값이 즉시 생성되어 응답 DTO 변환 시 NPE(500에러)가 안 납니다.
        Notice savedNotice = noticeRepository.saveAndFlush(notice);

        String coverUrl = null;
        if (coverImage != null && !coverImage.isEmpty()) {
            NoticeFile cover = saveFileLocally(coverImage, savedNotice, true);
            coverUrl = "/api/notices/files/" + cover.getId();
        }

        boolean hasFile = false;
        if (files != null && !files.isEmpty()) {
            for (MultipartFile f : files) {
                if (!f.isEmpty()) {
                    saveFileLocally(f, savedNotice, false);
                    hasFile = true;
                }
            }
        }
        return NoticeResponse.fromEntity(savedNotice, coverUrl, hasFile);
    }

    // 2. 🌟 컴파일 에러 해결: 목록 조회 메서드 구현
    @Transactional(readOnly = true)
    public List<NoticeResponse> getNotices(String category, String keyword) {
        List<Notice> notices;

        if (keyword != null && !keyword.trim().isEmpty()) {
            notices = noticeRepository.searchByKeyword(keyword);
        } else if (category != null && !category.equals("전체")) {
            notices = noticeRepository.findByCategoryOrderByCreatedAtDesc(category);
        } else {
            notices = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        return notices.stream().map(n -> {
            String coverUrl = n.getFiles().stream()
                    .filter(NoticeFile::isCover)
                    .findFirst()
                    .map(f -> "/api/notices/files/" + f.getId())
                    .orElse(null);
            boolean hasFile = n.getFiles().stream().anyMatch(f -> !f.isCover());
            return NoticeResponse.fromEntity(n, coverUrl, hasFile);
        }).collect(Collectors.toList());
    }

    // 3. 상세 조회 (조회수 증가)
    public NoticeResponse getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 1. 조회수 증가 호출
        notice.addViewCount();

        noticeRepository.saveAndFlush(notice);

        String coverUrl = notice.getFiles().stream()
                .filter(NoticeFile::isCover)
                .findFirst()
                .map(f -> "/api/notices/files/" + f.getId())
                .orElse(null);

        boolean hasFile = notice.getFiles().stream().anyMatch(f -> !f.isCover());

        return NoticeResponse.fromEntity(notice, coverUrl, hasFile);
    }

    // 4. 🌟 삭제 권한: 본인 또는 관리자(ADMIN)만 가능
    public void deleteNotice(Long id, String loginId) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        User currentUser = userRepository.findByLoginId(loginId).orElseThrow();

        boolean isOwner = notice.getWriter().getLoginId().equals(loginId);
        boolean isAdmin = currentUser.getRole() == UserRole.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("본인 또는 관리자만 삭제 가능합니다.");
        }
        noticeRepository.delete(notice);
    }
    //수정
    public NoticeResponse updateNotice(Long id, NoticeRequest request, MultipartFile coverImage, List<MultipartFile> files, String loginId) throws IOException {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 🌟 권한 체크 (본인 또는 관리자만 수정 가능)
        User currentUser = userRepository.findByLoginId(loginId).orElseThrow();
        if (!notice.getWriter().getLoginId().equals(loginId) && currentUser.getRole() != UserRole.ROLE_ADMIN) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 1. 기본 정보 업데이트 (Dirty Checking 활용)
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCategory(request.getCategory());

        // 2. 새 커버 이미지가 올라온 경우 처리
        if (coverImage != null && !coverImage.isEmpty()) {
            // 기존 커버 이미지 정보가 있다면 삭제하거나 isCover=false 처리 (여기서는 삭제 예시)
            notice.getFiles().removeIf(NoticeFile::isCover);
            saveFileLocally(coverImage, notice, true);
        }

        // 3. 새 첨부파일이 올라온 경우 추가
        if (files != null && !files.isEmpty()) {
            for (MultipartFile f : files) {
                if (!f.isEmpty()) saveFileLocally(f, notice, false);
            }
        }

        Notice updatedNotice = noticeRepository.saveAndFlush(notice);

        // Response 반환 로직 (기존과 동일)
        String coverUrl = updatedNotice.getFiles().stream()
                .filter(NoticeFile::isCover).findFirst()
                .map(f -> "/api/notices/files/" + f.getId()).orElse(null);
        boolean hasFile = updatedNotice.getFiles().stream().anyMatch(f -> !f.isCover());

        return NoticeResponse.fromEntity(updatedNotice, coverUrl, hasFile);
    }

    // 5. 댓글 등록
    public NoticeResponse.CommentDto addComment(Long noticeId, NoticeCommentRequest request, String loginId) {
        Notice notice = noticeRepository.findById(noticeId).orElseThrow();
        User writer = userRepository.findByLoginId(loginId).orElseThrow();

        NoticeComment comment = NoticeComment.builder()
                .notice(notice).writer(writer).content(request.getContent()).build();

        NoticeComment saved = noticeCommentRepository.saveAndFlush(comment);

        String dept = (saved.getWriter().getDepartment() != null) ? saved.getWriter().getDepartment().getDeptName() : "소속없음";
        return NoticeResponse.CommentDto.builder()
                .id(saved.getId()).dept(dept).rank(saved.getWriter().getPosition())
                .user(saved.getWriter().getName()).text(saved.getContent())
                .date("방금 전").build();
    }


    private NoticeFile saveFileLocally(MultipartFile file, Notice notice, boolean isCover) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // 1. 🌟 경로를 절대 경로로 변환하고 정규화합니다. (C:/uploads/notices)
        java.nio.file.Path rootPath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();

        // 2. 🌟 폴더가 없으면 실제 디스크에 생성합니다.
        java.io.File directory = rootPath.toFile();
        if (!directory.exists()) {
            directory.mkdirs(); // 하위 폴더까지 한 번에 생성
        }

        // 3. 🌟 저장할 최종 파일 객체 생성
        java.io.File destination = new File(directory, savedFilename);

        // 4. 🌟 파일 저장 (톰캣 임시폴더가 아닌 지정한 C:/uploads/notices에 저장됨)
        file.transferTo(destination);

        NoticeFile noticeFile = NoticeFile.builder()
                .notice(notice)
                .originalFileName(originalFilename)
                .savedFileName(savedFilename)
                .filePath(destination.getAbsolutePath()) // DB에는 실제 저장된 전체 경로 기록
                .fileSize(file.getSize())
                .isCover(isCover)
                .build();

        return noticeFileRepository.save(noticeFile);
    }
}