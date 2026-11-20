package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class AppUserConstraintsTest {

	@Autowired
	AppUserRepository repository;

	@Test
	void shouldRejectNullEmail() {
		AppUser user = new AppUser();
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);

		assertThatThrownBy(() -> {
			repository.save(user);
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void shouldRejectInvalidRole() {
		AppUser user = new AppUser();
		user.setEmail("test@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setStatus(UserStatus.ACTIVE);

		assertThatThrownBy(() -> {
			repository.save(user);
		}).isInstanceOf(Exception.class);
	}

	@Test
	void shouldRejectInvalidStatus() {
		AppUser user = new AppUser();
		user.setEmail("test@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setRole(UserRole.CUSTOMER);

		assertThatThrownBy(() -> {
			repository.save(user);
		}).isInstanceOf(Exception.class);
	}

	@Test
	void shouldSetCreatedAtOnPersist() {
		AppUser user = new AppUser();
		user.setEmail("timestamp@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);

		AppUser saved = repository.save(user);
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void shouldStartWithEmailNotVerified() {
		AppUser user = new AppUser();
		user.setEmail("notverified@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);

		AppUser saved = repository.save(user);
		assertThat(saved.getEmailVerified()).isFalse();
	}

	@Test
	void shouldUpdateEmailVerificationStatus() {
		AppUser user = new AppUser();
		user.setEmail("verify@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);
		user.setEmailVerified(false);

		AppUser saved = repository.save(user);
		saved.setEmailVerified(true);
		repository.save(saved);

		AppUser updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getEmailVerified()).isTrue();
	}

	@Test
	void shouldStoreLicensePlate() {
		AppUser user = new AppUser();
		user.setEmail("license@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Test");
		user.setLastName("User");
		user.setRole(UserRole.MERCHANT);
		user.setStatus(UserStatus.ACTIVE);
		user.setLicensePlate("AB-123-CD");

		AppUser saved = repository.save(user);

		AppUser retrieved = repository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getLicensePlate()).isEqualTo("AB-123-CD");
	}
}
