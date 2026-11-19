package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.PermissionType;
import com.shoploc.user.entity.UserPermission;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class UserPermissionRepositoryTest {

	@Autowired
	UserPermissionRepository repository;

	@Autowired
	AppUserRepository appUserRepository;

	private AppUser admin;
	private AppUser user;

	@BeforeEach
	void setup() {
		admin = new AppUser();
		admin.setEmail("admin@example.com");
		admin.setPasswordHash("pass");
		admin.setFirstName("Admin");
		admin.setLastName("User");
		admin.setRole(UserRole.ASSOCIATION);
		admin.setStatus(UserStatus.ACTIVE);
		admin = appUserRepository.save(admin);

		user = new AppUser();
		user.setEmail("user@example.com");
		user.setPasswordHash("pass");
		user.setFirstName("Regular");
		user.setLastName("User");
		user.setRole(UserRole.MERCHANT);
		user.setStatus(UserStatus.ACTIVE);
		user = appUserRepository.save(user);
	}

	@Test
	void shouldSaveAndRetrievePermission() {
		UserPermission perm = new UserPermission();
		perm.setUser(user);
		perm.setGrantedBy(admin);
		perm.setPermission(PermissionType.VALIDATE_MERCHANTS);

		UserPermission saved = repository.save(perm);

		UserPermission retrieved = repository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getPermission()).isEqualTo(PermissionType.VALIDATE_MERCHANTS);
		assertThat(retrieved.getUser().getId()).isEqualTo(user.getId());
		assertThat(retrieved.getGrantedBy().getId()).isEqualTo(admin.getId());
	}

	@Test
	void shouldListPermissions() {
		UserPermission perm1 = new UserPermission();
		perm1.setUser(user);
		perm1.setGrantedBy(admin);
		perm1.setPermission(PermissionType.VALIDATE_MERCHANTS);

		UserPermission perm2 = new UserPermission();
		perm2.setUser(user);
		perm2.setGrantedBy(admin);
		perm2.setPermission(PermissionType.CONFIGURE_PROGRAM);

		repository.save(perm1);
		repository.save(perm2);

		var allPermissions = repository.findAll();
		assertThat(allPermissions).hasSize(2);
	}
}
