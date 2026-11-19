package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class AppUserAllFieldsTest {

	@Autowired
	AppUserRepository repository;

	@Test
	void shouldSetAndGetAllFields() {
		AppUser user = new AppUser();
		user.setEmail("allfields@example.com");
		user.setPasswordHash("hashed_password_12345");
		user.setFirstName("All");
		user.setLastName("Fields");
		user.setRole(UserRole.TOWN_HALL);
		user.setStatus(UserStatus.DISABLED);
		user.setEmailVerified(true);
		user.setLicensePlate("XX-123-YY");

		AppUser saved = repository.save(user);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getEmail()).isEqualTo("allfields@example.com");
		assertThat(saved.getPasswordHash()).isEqualTo("hashed_password_12345");
		assertThat(saved.getFirstName()).isEqualTo("All");
		assertThat(saved.getLastName()).isEqualTo("Fields");
		assertThat(saved.getRole()).isEqualTo(UserRole.TOWN_HALL);
		assertThat(saved.getStatus()).isEqualTo(UserStatus.DISABLED);
		assertThat(saved.getEmailVerified()).isTrue();
		assertThat(saved.getLicensePlate()).isEqualTo("XX-123-YY");
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getVersion()).isNotNull();
	}

	@Test
	void shouldTestAllUserRolesAndStatuses() {
		for (UserRole role : UserRole.values()) {
			for (UserStatus status : UserStatus.values()) {
				AppUser user = new AppUser();
				user.setEmail(role + "_" + status + "@example.com");
				user.setPasswordHash("pass");
				user.setFirstName("Test");
				user.setLastName("User");
				user.setRole(role);
				user.setStatus(status);

				AppUser saved = repository.save(user);

				AppUser retrieved = repository.findById(saved.getId()).orElse(null);
				assertThat(retrieved).isNotNull();
				assertThat(retrieved.getRole()).isEqualTo(role);
				assertThat(retrieved.getStatus()).isEqualTo(status);
			}
		}
	}

	@Test
	void shouldUpdateMultipleFieldsConcurrently() {
		AppUser user = new AppUser();
		user.setEmail("multiupdate@example.com");
		user.setPasswordHash("pass1");
		user.setFirstName("Original");
		user.setLastName("Name");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.PENDING);
		user.setEmailVerified(false);

		AppUser saved = repository.save(user);

		saved.setPasswordHash("pass2");
		saved.setFirstName("Updated");
		saved.setStatus(UserStatus.ACTIVE);
		saved.setEmailVerified(true);
		saved.setLicensePlate("NEW-PLATE");
		repository.save(saved);

		AppUser updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getPasswordHash()).isEqualTo("pass2");
		assertThat(updated.getFirstName()).isEqualTo("Updated");
		assertThat(updated.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(updated.getEmailVerified()).isTrue();
		assertThat(updated.getLicensePlate()).isEqualTo("NEW-PLATE");
	}
}
