package com.projectx.backend.snapshot.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectx.backend.snapshot.domain.entity.DataSnapshotAsset;
import com.projectx.backend.snapshot.domain.entity.DataSnapshotAssetId;

public interface DataSnapshotAssetRepository extends JpaRepository<DataSnapshotAsset, DataSnapshotAssetId> {

	List<DataSnapshotAsset> findByIdDataSnapshotId(Long dataSnapshotId);

}
