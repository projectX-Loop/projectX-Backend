package com.projectx.backend.asset.application;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectx.backend.asset.api.UniverseResponse;
import com.projectx.backend.asset.api.UniverseResponse.Snapshot;
import com.projectx.backend.asset.api.UniverseResponse.UniverseAsset;
import com.projectx.backend.asset.api.UniverseResponse.Window;
import com.projectx.backend.asset.domain.repository.InvAssetRepository;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.snapshot.domain.entity.DataSnapshot;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotAssetRepository;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UniverseQueryService {

	private final DataSnapshotRepository dataSnapshotRepository;
	private final DataSnapshotAssetRepository dataSnapshotAssetRepository;
	private final InvAssetRepository invAssetRepository;

	@Transactional(readOnly = true)
	public UniverseResponse getUniverse() {
		DataSnapshot dataSnapshot = dataSnapshotRepository.findByIsCurrentTrue()
				.orElseThrow(() -> new BusinessException(ErrorCode.DATA_SNAPSHOT_UNAVAILABLE));
		Set<Long> assetIds = dataSnapshotAssetRepository.findByIdDataSnapshotId(dataSnapshot.getId()).stream()
				.map(snapshotAsset -> snapshotAsset.getId().getInvAssetId())
				.collect(java.util.stream.Collectors.toSet());
		List<UniverseAsset> assets = invAssetRepository.findAllById(assetIds).stream()
				.map(asset -> new UniverseAsset(asset.getCode(), asset.getDisplayName(), asset.getInstrument(),
						asset.getTaxClass()))
				.sorted(java.util.Comparator.comparing(UniverseAsset::code))
				.toList();
		YearMonth start = YearMonth.from(dataSnapshot.getStartMonth());
		YearMonth end = YearMonth.from(dataSnapshot.getLatestMonth());
		Window window = new Window(start.toString(), end.toString(), (int) ChronoUnit.MONTHS.between(start, end) + 1);
		return new UniverseResponse(new Snapshot(dataSnapshot.getDataVersion(), dataSnapshot.getDataHash(), window), assets);
	}

}
