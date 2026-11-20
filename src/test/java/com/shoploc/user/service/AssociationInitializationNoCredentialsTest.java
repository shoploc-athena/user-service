package com.shoploc.user.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
	"ASSOCIATION_ADMIN_EMAIL=",
	"ASSOCIATION_ADMIN_PASSWORD="
})
class AssociationInitializationNoCredentialsTest {

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private UserPermissionRepository userPermissionRepository;

	@Autowired
	private AssociationInitializationService service;

	@Test
	void testNothingCreatedWithoutCredentials() {
		service.initializeFirstAssociationAccount();

		long userCount = appUserRepository.count();
		long permissionCount = userPermissionRepository.count();

		assertEquals(0, userCount, "No user should be created without credentials");
		assertEquals(0, permissionCount, "No permission should be created without credentials");
	}

	@Test
	void testNothingCreatedWithMissingEmail() {
		long userCountBefore = appUserRepository.count();

		service.initializeFirstAssociationAccount();

		long userCountAfter = appUserRepository.count();
		assertEquals(userCountBefore, userCountAfter, "No user should be created with missing email");
	}

	@Test
	void testNothingCreatedWithMissingPassword() {
		long userCountBefore = appUserRepository.count();

		service.initializeFirstAssociationAccount();

		long userCountAfter = appUserRepository.count();
		assertEquals(userCountBefore, userCountAfter, "No user should be created with missing password");
	}
}
