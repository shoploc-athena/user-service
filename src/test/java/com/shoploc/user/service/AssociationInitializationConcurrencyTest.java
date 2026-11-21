package com.shoploc.user.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
	"ASSOCIATION_ADMIN_EMAIL=admin@association.com",
	"ASSOCIATION_ADMIN_PASSWORD=ConcurrentTest123!"
})
class AssociationInitializationConcurrencyTest {

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private AssociationInitializationService service;

	@Test
	void testSimultaneousStartupDoesNotFail() {
		service.initializeFirstAssociationAccount();
		service.initializeFirstAssociationAccount();

		long associationCount = appUserRepository.findAll().stream()
			.filter(u -> u.getRole() == UserRole.ASSOCIATION)
			.count();

		assertEquals(1, associationCount, "Only one ASSOCIATION account should exist");
	}

	@Test
	void testExceptionHandlingForConcurrentCreation() {
		service.initializeFirstAssociationAccount();

		long userCount = appUserRepository.count();
		assertEquals(1, userCount, "One user should exist");

		service.initializeFirstAssociationAccount();

		long userCountAfter = appUserRepository.count();
		assertEquals(1, userCountAfter, "Still only one user after second call");
	}
}
