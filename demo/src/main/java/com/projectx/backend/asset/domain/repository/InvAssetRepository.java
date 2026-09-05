package com.projectx.backend.asset.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectx.backend.asset.domain.entity.InvAsset;

public interface InvAssetRepository extends JpaRepository<InvAsset, Long> {

	Optional<InvAsset> findByCode(String code);

}
