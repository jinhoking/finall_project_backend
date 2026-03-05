package com.boot.security.repository;

import com.boot.security.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetNumber(String assetNumber);
}