package com.shoploc.user.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
	"ASSOCIATION_ADMIN_EMAIL=concurrent@association.com",
	"ASSOCIATION_ADMIN_PASSWORD=ConcurrentPassword123!"
})
class AssociationInitializationConcurrentThreadTest {

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private UserPermissionRepository userPermissionRepository;

	@Autowired
	private AssociationInitializationService service;

	@AfterEach
	void cleanup() {
		userPermissionRepository.deleteAll();
		appUserRepository.deleteAll();
	}

	@Test
	void testSequentialCallsAreIdempotent() {
		service.initializeFirstAssociationAccount();
		long countAfterFirst = appUserRepository.count();

		service.initializeFirstAssociationAccount();
		long countAfterSecond = appUserRepository.count();

		assertEquals(countAfterFirst, countAfterSecond, "Second call should not create duplicate users");

		long associationCount = appUserRepository.findAll().stream()
			.filter(u -> u.getRole() == UserRole.ASSOCIATION)
			.count();

		assertEquals(1, associationCount, "Only one ASSOCIATION account should exist");
	}

	@Test
	void testIdempotenceWithMultipleCalls() {
		for (int i = 0; i < 3; i++) {
			service.initializeFirstAssociationAccount();
		}

		long associationCount = appUserRepository.findAll().stream()
			.filter(u -> u.getRole() == UserRole.ASSOCIATION)
			.count();

		assertEquals(1, associationCount, "Only one ASSOCIATION account should exist after multiple calls");

		long permissionCount = userPermissionRepository.findAll().size();
		assertEquals(2, permissionCount, "Only two permissions should exist (MANAGE_AGENTS and VALIDATE_MERCHANTS)");
	}
}
