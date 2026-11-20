package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import java.util.UUID;
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
class AppUserRepositoryTest {

	@Autowired
	AppUserRepository repository;

	@Test
	void shouldSaveAndRetrieveAppUser() {
		AppUser user = new AppUser();
		user.setEmail("test@example.com");
		user.setPasswordHash("hashed_password");
		user.setFirstName("John");
		user.setLastName("Doe");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);
		user.setEmailVerified(false);

		AppUser saved = repository.save(user);

		AppUser retrieved = repository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getEmail()).isEqualTo("test@example.com");
		assertThat(retrieved.getFirstName()).isEqualTo("John");
		assertThat(retrieved.getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(retrieved.getStatus()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	void shouldUpdateAppUser() {
		AppUser user = new AppUser();
		user.setEmail("update@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Update");
		user.setLastName("Test");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.PENDING);

		AppUser saved = repository.save(user);

		saved.setStatus(UserStatus.ACTIVE);
		saved.setFirstName("Updated");
		repository.save(saved);

		AppUser updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(updated.getFirstName()).isEqualTo("Updated");
	}

	@Test
	void shouldFindByEmail() {
		AppUser user = new AppUser();
		user.setEmail("findme@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Find");
		user.setLastName("Me");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);

		repository.save(user);

		var found = repository.findByEmail("findme@example.com");
		assertThat(found).isPresent();
		assertThat(found.get().getFirstName()).isEqualTo("Find");
	}
}
