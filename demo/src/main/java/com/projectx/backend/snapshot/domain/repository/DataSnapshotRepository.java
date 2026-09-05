package com.projectx.backend.snapshot.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projectx.backend.snapshot.domain.entity.DataSnapshot;

public interface DataSnapshotRepository extends JpaRepository<DataSnapshot, Long> {

	Optional<DataSnapshot> findByIsCurrentTrue();

	Optional<DataSnapshot> findByDataHash(String dataHash);

}
