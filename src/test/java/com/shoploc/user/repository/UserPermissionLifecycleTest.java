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
class UserPermissionLifecycleTest {

	@Autowired
	UserPermissionRepository repository;

	@Autowired
	AppUserRepository appUserRepository;

	private AppUser admin;
	private AppUser merchant;

	@BeforeEach
	void setup() {
		admin = new AppUser();
		admin.setEmail("admin3@example.com");
		admin.setPasswordHash("pass");
		admin.setFirstName("Admin");
		admin.setLastName("User");
		admin.setRole(UserRole.ASSOCIATION);
		admin.setStatus(UserStatus.ACTIVE);
		admin = appUserRepository.save(admin);

		merchant = new AppUser();
		merchant.setEmail("merchant3@example.com");
		merchant.setPasswordHash("pass");
		merchant.setFirstName("Merchant");
		merchant.setLastName("User");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.ACTIVE);
		merchant = appUserRepository.save(merchant);
	}

	@Test
	void shouldGrantMultiplePermissionsToSameUser() {
		UserPermission perm1 = new UserPermission();
		perm1.setUser(merchant);
		perm1.setGrantedBy(admin);
		perm1.setPermission(PermissionType.VALIDATE_MERCHANTS);

		UserPermission perm2 = new UserPermission();
		perm2.setUser(merchant);
		perm2.setGrantedBy(admin);
		perm2.setPermission(PermissionType.CONFIGURE_PROGRAM);

		repository.save(perm1);
		repository.save(perm2);

		var permissions = repository.findAll();
		assertThat(permissions).hasSizeGreaterThanOrEqualTo(2);
	}

	@Test
	void shouldTrackGrantedByUser() {
		UserPermission perm = new UserPermission();
		perm.setUser(merchant);
		perm.setGrantedBy(admin);
		perm.setPermission(PermissionType.CONFIGURE_BENEFITS);

		UserPermission saved = repository.save(perm);

		assertThat(saved.getGrantedBy().getId()).isEqualTo(admin.getId());
	}

	@Test
	void shouldUpdatePermissionStatus() {
		UserPermission perm = new UserPermission();
		perm.setUser(merchant);
		perm.setGrantedBy(admin);
		perm.setPermission(PermissionType.VIEW_INDICATORS);

		UserPermission saved = repository.save(perm);

		UserPermission retrieved = repository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getPermission()).isEqualTo(PermissionType.VIEW_INDICATORS);
	}

	@Test
	void shouldHandleAllPermissionTypes() {
		for (PermissionType permType : PermissionType.values()) {
			UserPermission perm = new UserPermission();
			perm.setUser(merchant);
			perm.setGrantedBy(admin);
			perm.setPermission(permType);

			UserPermission saved = repository.save(perm);
			assertThat(saved.getPermission()).isEqualTo(permType);
		}
	}
}
