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
	@Transactional
	void testTwoThreadsStartingSimultaneouslyDoNotCrash() throws InterruptedException {
		CyclicBarrier barrier = new CyclicBarrier(2);
		AtomicReference<Exception> thread1Exception = new AtomicReference<>();
		AtomicReference<Exception> thread2Exception = new AtomicReference<>();

		Thread t1 = new Thread(() -> {
			try {
				barrier.await();
				service.initializeFirstAssociationAccount();
			} catch (Exception e) {
				thread1Exception.set(e);
			}
		});

		Thread t2 = new Thread(() -> {
			try {
				barrier.await();
				service.initializeFirstAssociationAccount();
			} catch (Exception e) {
				thread2Exception.set(e);
			}
		});

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		assertNull(thread1Exception.get(), "Thread 1 should not throw exception");
		assertNull(thread2Exception.get(), "Thread 2 should not throw exception");

		long associationCount = appUserRepository.findAll().stream()
			.filter(u -> u.getRole() == UserRole.ASSOCIATION)
			.count();

		assertEquals(1, associationCount, "Only one ASSOCIATION account should exist");
	}

	@Test
	@Transactional
	void testMultipleThreadsWithTransactionalContext() throws InterruptedException {
		CyclicBarrier barrier = new CyclicBarrier(3);
		AtomicReference<Exception> exception1 = new AtomicReference<>();
		AtomicReference<Exception> exception2 = new AtomicReference<>();
		AtomicReference<Exception> exception3 = new AtomicReference<>();

		Thread t1 = new Thread(() -> {
			try {
				barrier.await();
				service.initializeFirstAssociationAccount();
			} catch (Exception e) {
				exception1.set(e);
			}
		});

		Thread t2 = new Thread(() -> {
			try {
				barrier.await();
				service.initializeFirstAssociationAccount();
			} catch (Exception e) {
				exception2.set(e);
			}
		});

		Thread t3 = new Thread(() -> {
			try {
				barrier.await();
				service.initializeFirstAssociationAccount();
			} catch (Exception e) {
				exception3.set(e);
			}
		});

		t1.start();
		t2.start();
		t3.start();
		t1.join();
		t2.join();
		t3.join();

		assertNull(exception1.get(), "Thread 1 should not throw exception");
		assertNull(exception2.get(), "Thread 2 should not throw exception");
		assertNull(exception3.get(), "Thread 3 should not throw exception");

		long associationCount = appUserRepository.findAll().stream()
			.filter(u -> u.getRole() == UserRole.ASSOCIATION)
			.count();

		assertEquals(1, associationCount, "Only one ASSOCIATION account should exist after 3 concurrent threads");
	}
}
