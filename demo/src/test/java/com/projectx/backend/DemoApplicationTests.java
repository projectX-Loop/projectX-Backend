package com.projectx.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

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
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Flyway flyway;

	@Test
	void contextLoads() {
	}

	@Test
	void seedsCurrentMvpScenario() {
		assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM data_snapshot WHERE is_current = TRUE", Integer.class))
				.isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("""
				SELECT safe_rate_annual_pct
				FROM data_snapshot
				WHERE data_hash = 'sha256:mvp-2021-08-2026-07-v1'
				""", BigDecimal.class)).isEqualByComparingTo("3.00");
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
