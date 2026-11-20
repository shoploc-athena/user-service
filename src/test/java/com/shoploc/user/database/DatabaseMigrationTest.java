package com.shoploc.user.database;

import static org.assertj.core.api.Assertions.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class DatabaseMigrationTest {

	@Autowired
	DataSource dataSource;

	@Test
	void shouldHaveAppUserTable() throws Exception {
		try (var connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet tables = metaData.getTables(null, null, "app_user", null);
			assertThat(tables.next()).isTrue();
		}
	}

	@Test
	void shouldHaveUserPermissionTable() throws Exception {
		try (var connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet tables = metaData.getTables(null, null, "user_permission", null);
			assertThat(tables.next()).isTrue();
		}
	}

	@Test
	void shouldHaveMerchantRequestTable() throws Exception {
		try (var connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			ResultSet tables = metaData.getTables(null, null, "merchant_request", null);
			assertThat(tables.next()).isTrue();
		}
	}
}
