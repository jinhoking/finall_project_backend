package com.boot.security.service;

import com.boot.security.dto.AssetResponse;
import com.boot.security.entity.Asset;
import com.boot.security.entity.AssetHistory;
import com.boot.security.entity.Project;
import com.boot.security.entity.User;
import com.boot.security.repository.AssetRepository;
import com.boot.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(AssetResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerAsset(Map<String, Object> request) {
        // 🌟 프론트에서 보낸 'holder' 키를 정상적으로 받습니다.
        String holderInput = (String) request.get("holder");
        User holder = userRepository.findByName(holderInput).orElse(null);

        // 🌟 [수정] count() 대신 밀리초를 사용해 절대 중복되지 않는 고유 번호 생성!
        String assetNumber = "AST-" + System.currentTimeMillis();

        String priceStr = String.valueOf(request.get("price")).replace(",", "");
        Long price = priceStr.isEmpty() || "null".equals(priceStr) ? 0L : Long.parseLong(priceStr);

        Asset asset = Asset.builder()
                .assetNumber(assetNumber)
                .name((String) request.get("name"))
                .category((String) request.get("category"))
                .status("정상")
                .sn((String) request.get("sn"))
                .price(price)
                .location((String) request.get("location"))
                .warranty((String) request.get("warranty"))
                .joinDate(LocalDateTime.now())
                .holder(holder)
                .holderName(holderInput) // 🌟 입력한 이름 정상 저장
                .build();

        AssetHistory history = AssetHistory.builder()
                .title("신규 자산 등록 및 입고")
                .eventDate(LocalDateTime.now())
                .adminName("관리자")
                .asset(asset)
                .build();

        asset.getHistory().add(history);
        assetRepository.save(asset);
    }

    // 🌟 [수정됨] 매개변수를 String assetNumber로 변경
    @Transactional
    public void addHistory(String assetNumber, Map<String, String> request) {
        Asset asset = assetRepository.findByAssetNumber(assetNumber)
                .orElseThrow(() -> new IllegalArgumentException("자산을 찾을 수 없습니다: " + assetNumber));

        AssetHistory history = AssetHistory.builder()
                .title(request.get("title"))
                .eventDate(LocalDateTime.now())
                .adminName(request.get("admin"))
                .asset(asset)
                .build();

        asset.getHistory().add(history);
        assetRepository.save(asset);
    }

    // 🌟 [수정됨] 매개변수를 String assetNumber로 변경
    @Transactional
    public void requestRepair(String assetNumber, Map<String, String> request) {
        Asset asset = assetRepository.findByAssetNumber(assetNumber)
                .orElseThrow(() -> new IllegalArgumentException("자산을 찾을 수 없습니다: " + assetNumber));

        asset.setStatus("수리중");

        String reason = request.get("reason");
        AssetHistory history = AssetHistory.builder()
                .title("수리 요청 - " + reason)
                .eventDate(LocalDateTime.now())
                .adminName(request.get("requester"))
                .asset(asset)
                .build();

        asset.getHistory().add(history);
        assetRepository.save(asset);
    }


    @Transactional
    public void updateAsset(String assetNumber, Map<String, Object> request) {
        Asset asset = assetRepository.findByAssetNumber(assetNumber)
                .orElseThrow(() -> new IllegalArgumentException("자산을 찾을 수 없습니다: " + assetNumber));

        // 넘어온 데이터가 있을 때만 업데이트 처리
        if (request.containsKey("name")) asset.setName((String) request.get("name"));
        if (request.containsKey("category")) asset.setCategory((String) request.get("category"));
        if (request.containsKey("sn")) asset.setSn((String) request.get("sn"));
        if (request.containsKey("location")) asset.setLocation((String) request.get("location"));
        if (request.containsKey("warranty")) asset.setWarranty((String) request.get("warranty"));
        if (request.containsKey("status")) asset.setStatus((String) request.get("status"));
        if (request.containsKey("holderName")) asset.setHolderName((String) request.get("holderName"));

        // 가격 파싱 (콤마 제거 후 Long 변환)
        if (request.containsKey("price")) {
            String priceStr = String.valueOf(request.get("price")).replace(",", "");
            asset.setPrice(priceStr.isEmpty() || "null".equals(priceStr) ? 0L : Long.parseLong(priceStr));
        }
    }
    @Transactional
    public void deleteAsset(String assetNumber) { // 🌟 Long id 대신 String assetNumber
        Asset asset = assetRepository.findByAssetNumber(assetNumber)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 자산이 없습니다: " + assetNumber));
        assetRepository.delete(asset);
    }


}