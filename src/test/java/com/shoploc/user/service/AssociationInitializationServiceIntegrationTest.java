package com.shoploc.user.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.PermissionType;
import com.shoploc.user.entity.UserPermission;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
	"ASSOCIATION_ADMIN_EMAIL=admin@association.com",
	"ASSOCIATION_ADMIN_PASSWORD=SecurePassword123!"
})
class AssociationInitializationServiceIntegrationTest {

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private UserPermissionRepository userPermissionRepository;

	@Autowired
	private AssociationInitializationService service;

	@Test
	void testFirstStartupCreatesAssociationAccount() {
		service.initializeFirstAssociationAccount();

		AppUser createdUser = appUserRepository.findByRole(UserRole.ASSOCIATION).orElse(null);
		assertNotNull(createdUser, "Association account should be created");
		assertEquals(UserRole.ASSOCIATION, createdUser.getRole());
		assertEquals(UserStatus.ACTIVE, createdUser.getStatus());
		assertTrue(createdUser.getEmailVerified());
		assertEquals("Association", createdUser.getFirstName());
		assertEquals("Admin", createdUser.getLastName());
	}

	@Test
	void testPasswordIsHashedWithBCrypt() {
		service.initializeFirstAssociationAccount();

		AppUser createdUser = appUserRepository.findByRole(UserRole.ASSOCIATION).orElse(null);
		assertNotNull(createdUser);
		String passwordHash = createdUser.getPasswordHash();
		assertNotNull(passwordHash);
		assertTrue(passwordHash.startsWith("$2"), "Password should be hashed with BCrypt");

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		assertTrue(encoder.matches("SecurePassword123!", passwordHash));
	}

	@Test
	void testPermissionsCreated() {
		service.initializeFirstAssociationAccount();

		AppUser createdUser = appUserRepository.findByRole(UserRole.ASSOCIATION).orElse(null);
		assertNotNull(createdUser);

		List<UserPermission> permissions = userPermissionRepository.findAll();
		assertEquals(2, permissions.size(), "Should have exactly 2 permissions");

		boolean hasManageAgents = permissions.stream()
			.anyMatch(p -> p.getUser().getId().equals(createdUser.getId()) &&
					p.getPermission() == PermissionType.MANAGE_AGENTS);
		boolean hasValidateMerchants = permissions.stream()
			.anyMatch(p -> p.getUser().getId().equals(createdUser.getId()) &&
					p.getPermission() == PermissionType.VALIDATE_MERCHANTS);

		assertTrue(hasManageAgents, "Should have MANAGE_AGENTS permission");
		assertTrue(hasValidateMerchants, "Should have VALIDATE_MERCHANTS permission");

		permissions.forEach(p -> assertNull(p.getGrantedBy(), "granted_by should be null"));
	}

	@Test
	void testSecondStartupIsIdempotent() {
		service.initializeFirstAssociationAccount();

		int initialUserCount = (int) appUserRepository.count();
		int initialPermissionCount = (int) userPermissionRepository.count();

		service.initializeFirstAssociationAccount();

		int finalUserCount = (int) appUserRepository.count();
		int finalPermissionCount = (int) userPermissionRepository.count();

		assertEquals(initialUserCount, finalUserCount, "User count should not change on second run");
		assertEquals(initialPermissionCount, finalPermissionCount, "Permission count should not change on second run");
	}

	@Test
	void testNoPasswordOverwrite() {
		service.initializeFirstAssociationAccount();

		AppUser firstRun = appUserRepository.findByRole(UserRole.ASSOCIATION).orElse(null);
		String firstPasswordHash = firstRun.getPasswordHash();

		service.initializeFirstAssociationAccount();

		AppUser secondRun = appUserRepository.findByRole(UserRole.ASSOCIATION).orElse(null);
		String secondPasswordHash = secondRun.getPasswordHash();

		assertEquals(firstPasswordHash, secondPasswordHash, "Password should not be changed on second run");
	}

	@Test
	void testIdempotenceByRole() {
		service.initializeFirstAssociationAccount();

		AppUser firstAccount = appUserRepository.findByRole(UserRole.ASSOCIATION).orElse(null);
		assertNotNull(firstAccount);

		service.initializeFirstAssociationAccount();

		List<AppUser> allAssociation = appUserRepository.findAll().stream()
			.filter(u -> u.getRole() == UserRole.ASSOCIATION)
			.toList();

		assertEquals(1, allAssociation.size(), "Should have exactly one ASSOCIATION account");
		assertEquals(firstAccount.getId(), allAssociation.get(0).getId());
	}
}
