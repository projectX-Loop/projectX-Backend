package com.projectx.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.flywaydb.core.Flyway;

@SpringBootTest
@Testcontainers
class DemoApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("pgvector/pgvector:pg16-bookworm");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Flyway flyway;

	@Test
	void contextLoads() {
	}

	@Test
	void seedsCurrentAiEngineSnapshot() {
		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM data_snapshot WHERE is_current = TRUE", Integer.class))
				.isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("""
				SELECT data_hash
				FROM data_snapshot
				WHERE is_current = TRUE
				""", String.class)).isEqualTo("sha256:fa84100c67a0589aaa3cdb88a13138b87dba6417403f56608680896dbfddb90d");
		assertThat(jdbcTemplate.queryForObject("""
				SELECT safe_rate_annual_pct
				FROM data_snapshot
				WHERE is_current = TRUE
				""", BigDecimal.class)).isEqualByComparingTo("3.15");
		assertThat(jdbcTemplate.queryForObject("""
				SELECT start_month
				FROM data_snapshot
				WHERE is_current = TRUE
				""", LocalDate.class)).isEqualTo(LocalDate.of(2009, 8, 1));
		assertThat(jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM data_snapshot_asset
				WHERE data_snapshot_id = (SELECT id FROM data_snapshot WHERE is_current = TRUE)
				""", Integer.class)).isEqualTo(4);
		assertThat(jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM data_snapshot_asset_return
				WHERE data_snapshot_id = (
					SELECT id FROM data_snapshot WHERE data_hash = 'sha256:mvp-2021-08-2026-07-v1'
				)
				""", Integer.class)).isEqualTo(240);
		assertThat(flyway.migrate().migrationsExecuted).isZero();
	}

}
