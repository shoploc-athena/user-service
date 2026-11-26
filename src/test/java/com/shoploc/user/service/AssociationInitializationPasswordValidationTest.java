package com.shoploc.user.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
	"ASSOCIATION_ADMIN_EMAIL=admin@association.com",
	"ASSOCIATION_ADMIN_PASSWORD=short"
})
class AssociationInitializationPasswordValidationTest {

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private UserPermissionRepository userPermissionRepository;

	@Autowired
	private AssociationInitializationService service;

	@Test
	void testPasswordTooShortRejected() {
		service.initializeFirstAssociationAccount();

		long userCount = appUserRepository.count();
		long permissionCount = userPermissionRepository.count();

		assertEquals(0, userCount, "No user should be created with password < 12 chars");
		assertEquals(0, permissionCount, "No permissions should be created");
	}

	@Test
	void testExactlyMinimumPasswordLengthAccepted() {
		long userCountBefore = appUserRepository.count();
		assertEquals(0, userCountBefore);

		service.initializeFirstAssociationAccount();

		long userCountAfter = appUserRepository.count();
		assertEquals(0, userCountAfter, "Password of 5 chars should be rejected");
	}
}
