package com.projectx.backend.asset.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.projectx.backend.asset.api.UniverseResponse;
import com.projectx.backend.asset.domain.entity.InvAsset;
import com.projectx.backend.asset.domain.repository.InvAssetRepository;
import com.projectx.backend.global.exception.BusinessException;
import com.projectx.backend.global.exception.ErrorCode;
import com.projectx.backend.snapshot.domain.entity.DataSnapshot;
import com.projectx.backend.snapshot.domain.entity.DataSnapshotAsset;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotAssetRepository;
import com.projectx.backend.snapshot.domain.repository.DataSnapshotRepository;

import static org.mockito.Mockito.mock;

class UniverseQueryServiceTests {

	@Test
	void returnsOnlyAssetsLinkedToCurrentSnapshotInCodeOrder() {
		DataSnapshotRepository dataSnapshotRepository = mock(DataSnapshotRepository.class);
		DataSnapshotAssetRepository dataSnapshotAssetRepository = mock(DataSnapshotAssetRepository.class);
		InvAssetRepository invAssetRepository = mock(InvAssetRepository.class);
		UniverseQueryService service = new UniverseQueryService(dataSnapshotRepository, dataSnapshotAssetRepository,
				invAssetRepository);
		DataSnapshot snapshot = mock(DataSnapshot.class);
		given(snapshot.getId()).willReturn(99L);
		given(snapshot.getDataVersion()).willReturn("2026-09-02");
		given(snapshot.getDataHash()).willReturn("sha256:current");
		given(snapshot.getStartMonth()).willReturn(LocalDate.of(2021, 8, 1));
		given(snapshot.getLatestMonth()).willReturn(LocalDate.of(2026, 7, 1));
		DataSnapshotAsset linkedUsEquity = DataSnapshotAsset.create(snapshot, 2L, LocalDate.of(2021, 8, 1),
				LocalDate.of(2026, 7, 1));
		DataSnapshotAsset linkedKoreanEquity = DataSnapshotAsset.create(snapshot, 1L, LocalDate.of(2021, 8, 1),
				LocalDate.of(2026, 7, 1));

		given(dataSnapshotRepository.findByIsCurrentTrue()).willReturn(Optional.of(snapshot));
		given(dataSnapshotAssetRepository.findByIdDataSnapshotId(99L))
				.willReturn(List.of(linkedUsEquity, linkedKoreanEquity));
		given(invAssetRepository.findAllById(org.mockito.ArgumentMatchers.anySet())).willReturn(List.of(
				InvAsset.create("US_EQ", "SPY", "해외 주식", "USD", "foreign"),
				InvAsset.create("KR_EQ", "069500", "국내 주식", "KRW", "domestic")));

		UniverseResponse response = service.getUniverse();

		assertThat(response.snapshot().dataVersion()).isEqualTo("2026-09-02");
		assertThat(response.snapshot().window()).isEqualTo(new UniverseResponse.Window("2021-08", "2026-07", 60));
		assertThat(response.assets()).extracting(UniverseResponse.UniverseAsset::code).containsExactly("KR_EQ", "US_EQ");
	}

	@Test
	void returnsEmptyAssetsWhenCurrentSnapshotHasNoLinkedAsset() {
		DataSnapshotRepository dataSnapshotRepository = mock(DataSnapshotRepository.class);
		DataSnapshotAssetRepository dataSnapshotAssetRepository = mock(DataSnapshotAssetRepository.class);
		InvAssetRepository invAssetRepository = mock(InvAssetRepository.class);
		UniverseQueryService service = new UniverseQueryService(dataSnapshotRepository, dataSnapshotAssetRepository,
				invAssetRepository);
		DataSnapshot snapshot = mock(DataSnapshot.class);
		given(snapshot.getId()).willReturn(99L);
		given(snapshot.getDataVersion()).willReturn("2026-09-02");
		given(snapshot.getDataHash()).willReturn("sha256:current");
		given(snapshot.getStartMonth()).willReturn(LocalDate.of(2021, 8, 1));
		given(snapshot.getLatestMonth()).willReturn(LocalDate.of(2026, 7, 1));
		given(dataSnapshotRepository.findByIsCurrentTrue()).willReturn(Optional.of(snapshot));
		given(dataSnapshotAssetRepository.findByIdDataSnapshotId(99L)).willReturn(List.of());

		UniverseResponse response = service.getUniverse();

		assertThat(response.assets()).isEmpty();
	}

	@Test
	void rejectsRequestWhenCurrentSnapshotDoesNotExist() {
		DataSnapshotRepository dataSnapshotRepository = mock(DataSnapshotRepository.class);
		UniverseQueryService service = new UniverseQueryService(dataSnapshotRepository, mock(DataSnapshotAssetRepository.class),
				mock(InvAssetRepository.class));
		given(dataSnapshotRepository.findByIsCurrentTrue()).willReturn(Optional.empty());

		assertThatThrownBy(service::getUniverse)
				.isInstanceOf(BusinessException.class)
				.satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
						.isEqualTo(ErrorCode.DATA_SNAPSHOT_UNAVAILABLE));
	}

}
