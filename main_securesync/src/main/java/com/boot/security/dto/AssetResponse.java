package com.boot.security.dto;

import com.boot.security.entity.Asset;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetResponse {
    private String id;
    private String name;
    private String category;
    private String holder;
    private String status;
    private String joinDate;
    private String sn;
    private String price;
    private String location;
    private String warranty;
    private List<HistoryDto> history;

    @Data
    @AllArgsConstructor
    public static class HistoryDto {
        private String title;
        private String date;
        private String admin;
    }

    public AssetResponse(Asset asset) {
        this.id = asset.getAssetNumber();
        this.name = asset.getName();
        this.category = asset.getCategory();

        // 🌟 [수정됨] 무조건 입력했던 이름을 가져오도록 변경!
        this.holder = (asset.getHolderName() != null && !asset.getHolderName().isEmpty())
                ? asset.getHolderName() : "미지정";

        this.status = asset.getStatus();
        this.joinDate = asset.getJoinDate() != null ? asset.getJoinDate().toString().split("T")[0] : "";
        this.sn = asset.getSn();
        this.price = String.format("%,d", asset.getPrice() != null ? asset.getPrice() : 0);
        this.location = asset.getLocation();
        this.warranty = asset.getWarranty();
        this.history = asset.getHistory().stream()
                .map(h -> new HistoryDto(h.getTitle(), h.getEventDate().toString().split("T")[0], h.getAdminName()))
                .collect(Collectors.toList());
    }
}