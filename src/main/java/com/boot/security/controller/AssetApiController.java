package com.boot.security.controller;

import com.boot.security.dto.AssetResponse;
import com.boot.security.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.boot.security.annotation.AuditLog;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetApiController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @PostMapping
    @AuditLog(action = "새로운 사내 자산이 등록되었습니다.", type = "success")
    public ResponseEntity<String> registerAsset(@RequestBody Map<String, Object> request) {
        try {
            assetService.registerAsset(request);
            return ResponseEntity.ok("자산 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("등록 실패: " + e.getMessage());
        }
    }

    // 🌟 [수정됨] PathVariable을 String id 로 변경
    @PostMapping("/{id}/history")
    public ResponseEntity<String> addHistory(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            assetService.addHistory(id, request);
            return ResponseEntity.ok("이력이 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🌟 [수정됨] PathVariable을 String id 로 변경
    @PostMapping("/{id}/repair")
    public ResponseEntity<String> requestRepair(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            assetService.requestRepair(id, request);
            return ResponseEntity.ok("수리 요청이 접수되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{assetNumber}")
    @AuditLog(action = "사내 자산이 수정되었습니다.", type = "success")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> updateAsset(@PathVariable String assetNumber, @RequestBody Map<String, Object> request) {
        try {
            assetService.updateAsset(assetNumber, request);
            return ResponseEntity.ok(Map.of("message", "자산 정보가 성공적으로 수정되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("수정 실패: " + e.getMessage());
        }
    }
    @DeleteMapping("/{assetNumber}") // 🌟 경로 변수 명확화
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @AuditLog(action = "사내 자산이 삭제되었습니다.", type = "success")
    public ResponseEntity<?> deleteAsset(@PathVariable String assetNumber) { // 🌟 String 타입으로 수신
        try {
            assetService.deleteAsset(assetNumber);
            return ResponseEntity.ok(Map.of("message", "자산이 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("삭제 실패: " + e.getMessage());
        }
    }

}