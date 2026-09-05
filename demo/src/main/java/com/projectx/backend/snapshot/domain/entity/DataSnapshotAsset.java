package com.projectx.backend.snapshot.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_snapshot_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataSnapshotAsset {

	@EmbeddedId
	private DataSnapshotAssetId id;

	@MapsId("dataSnapshotId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "data_snapshot_id")
	private DataSnapshot dataSnapshot;

	@Column(name = "available_from_month", nullable = false)
	private LocalDate availableFromMonth;

	@Column(name = "available_to_month", nullable = false)
	private LocalDate availableToMonth;

	public static DataSnapshotAsset create(DataSnapshot dataSnapshot, Long invAssetId, LocalDate availableFromMonth,
			LocalDate availableToMonth) {
		DataSnapshotAsset dataSnapshotAsset = new DataSnapshotAsset();
		dataSnapshotAsset.id = new DataSnapshotAssetId(dataSnapshot.getId(), invAssetId);
		dataSnapshotAsset.dataSnapshot = dataSnapshot;
		dataSnapshotAsset.availableFromMonth = availableFromMonth;
		dataSnapshotAsset.availableToMonth = availableToMonth;
		return dataSnapshotAsset;
	}

}
