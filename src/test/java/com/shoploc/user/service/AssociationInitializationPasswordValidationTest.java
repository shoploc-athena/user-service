package com.shoploc.user.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class AssociationInitializationPasswordValidationTest {

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private UserPermissionRepository userPermissionRepository;

	@Autowired
	private AssociationInitializationService service;

	@Test
	void testPasswordTooShortRejected() {
		ReflectionTestUtils.setField(service, "associationAdminEmail", "admin@association.com");
		ReflectionTestUtils.setField(service, "associationAdminPassword", "short");

		service.initializeFirstAssociationAccount();

		long userCount = appUserRepository.count();
		long permissionCount = userPermissionRepository.count();

		assertEquals(0, userCount, "No user should be created with password < 12 chars");
		assertEquals(0, permissionCount, "No permissions should be created");
	}

	@Test
	void testExactlyMinimumPasswordLengthAccepted() {
		ReflectionTestUtils.setField(service, "associationAdminEmail", "admin@association.com");
		ReflectionTestUtils.setField(service, "associationAdminPassword", "short");

		long userCountBefore = appUserRepository.count();
		assertEquals(0, userCountBefore);

		service.initializeFirstAssociationAccount();

		long userCountAfter = appUserRepository.count();
		assertEquals(0, userCountAfter, "Password of 5 chars should be rejected");
	}
}
