package com.projectx.backend.snapshot.domain.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataSnapshotAssetId implements Serializable {

	@Column(name = "data_snapshot_id")
	private Long dataSnapshotId;

	@Column(name = "inv_asset_id")
	private Long invAssetId;

	public DataSnapshotAssetId(Long dataSnapshotId, Long invAssetId) {
		this.dataSnapshotId = dataSnapshotId;
		this.invAssetId = invAssetId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DataSnapshotAssetId that)) {
			return false;
		}
		return Objects.equals(dataSnapshotId, that.dataSnapshotId) && Objects.equals(invAssetId, that.invAssetId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataSnapshotId, invAssetId);
	}

}
