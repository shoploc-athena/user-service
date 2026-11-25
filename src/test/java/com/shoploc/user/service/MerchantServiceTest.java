package com.shoploc.user.service;

import static org.assertj.core.api.Assertions.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.dto.MerchantSignUpRequest;
import com.shoploc.user.dto.MerchantSignUpResponse;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.exception.EmailAlreadyExistsException;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.MerchantRequestRepository;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class MerchantServiceTest {

	@Autowired
	private MerchantService merchantService;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private MerchantRequestRepository merchantRequestRepository;

	@Test
	void shouldSignUpMerchant() {
		MerchantSignUpRequest request = new MerchantSignUpRequest(
			"merchant@test.com",
			"securepassword123",
			"John",
			"Doe",
			"My Store",
			"123 Main Street"
		);

		MerchantSignUpResponse response = merchantService.signUpMerchant(request);

		assertThat(response).isNotNull();
		assertThat(response.getRequestId()).isNotNull();
		assertThat(response.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);

		// Verify user in database
		var user = appUserRepository.findByEmail("merchant@test.com");
		assertThat(user).isPresent();
		assertThat(user.get().getFirstName()).isEqualTo("John");
		assertThat(user.get().getLastName()).isEqualTo("Doe");
		assertThat(user.get().getRole()).isEqualTo(UserRole.MERCHANT);
		assertThat(user.get().getStatus()).isEqualTo(UserStatus.PENDING);
		assertThat(user.get().getEmailVerified()).isFalse();

		// Verify merchant request was created
		var merchantReq = merchantRequestRepository.findByUser(user.get());
		assertThat(merchantReq).isPresent();
		assertThat(merchantReq.get().getId()).isEqualTo(response.getRequestId());
		assertThat(merchantReq.get().getStoreName()).isEqualTo("My Store");
		assertThat(merchantReq.get().getStoreAddress()).isEqualTo("123 Main Street");
	}

	@Test
	void shouldNormalizeEmailBeforeStorage() {
		MerchantSignUpRequest request = new MerchantSignUpRequest(
			"  MERCHANT@TEST.COM  ",
			"securepassword123",
			"John",
			"Doe",
			"My Store",
			"123 Main Street"
		);

		merchantService.signUpMerchant(request);

		// Email should be normalized in database
		var user = appUserRepository.findByEmail("merchant@test.com");
		assertThat(user).isPresent();
		assertThat(user.get().getEmail()).isEqualTo("merchant@test.com");
	}

	@Test
	void shouldRejectDuplicateEmail() {
		MerchantSignUpRequest request1 = new MerchantSignUpRequest(
			"duplicate@test.com",
			"securepassword1",
			"First",
			"User",
			"Store 1",
			"Address 1"
		);

		merchantService.signUpMerchant(request1);

		MerchantSignUpRequest request2 = new MerchantSignUpRequest(
			"DUPLICATE@TEST.COM",
			"securepassword2",
			"Second",
			"User",
			"Store 2",
			"Address 2"
		);

		assertThatThrownBy(() -> merchantService.signUpMerchant(request2))
			.isInstanceOf(EmailAlreadyExistsException.class);
	}

	@Test
	void shouldCreateMerchantWithPendingStatus() {
		MerchantSignUpRequest req = new MerchantSignUpRequest(
			"newmerchant@test.com",
			"securepassword123",
			"Jane",
			"Smith",
			"Boutique",
			"456 Oak Avenue"
		);

		MerchantSignUpResponse resp = merchantService.signUpMerchant(req);

		assertThat(resp.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);

		var pendingUser = appUserRepository.findByEmail("newmerchant@test.com").get();
		assertThat(pendingUser.getRole()).isEqualTo(UserRole.MERCHANT);
		assertThat(pendingUser.getStatus()).isEqualTo(UserStatus.PENDING);
		assertThat(pendingUser.getEmailVerified()).isFalse();

		var pendingReq = merchantRequestRepository.findByUser(pendingUser);
		assertThat(pendingReq).isPresent();
		assertThat(pendingReq.get().getStoreName()).isEqualTo("Boutique");
	}

	@Test
	void shouldHandleSimultaneousSignupWithSameEmailAs409() throws Exception {
		CyclicBarrier barrier = new CyclicBarrier(2);
		AtomicReference<Exception> thread1Exception = new AtomicReference<>();
		AtomicReference<Exception> thread2Exception = new AtomicReference<>();

		Thread t1 = new Thread(() -> {
			try {
				barrier.await();
				merchantService.signUpMerchant(new MerchantSignUpRequest(
					"concurrent@test.com", "securepassword123",
					"John", "Doe", "Store1", "Address1"
				));
			} catch (Exception e) {
				thread1Exception.set(e);
			}
		});

		Thread t2 = new Thread(() -> {
			try {
				barrier.await();
				merchantService.signUpMerchant(new MerchantSignUpRequest(
					"concurrent@test.com", "securepassword456",
					"Jane", "Smith", "Store2", "Address2"
				));
			} catch (Exception e) {
				thread2Exception.set(e);
			}
		});

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		// One thread should succeed, the other should fail with DataIntegrityViolationException
		boolean t1Failed = thread1Exception.get() instanceof DataIntegrityViolationException;
		boolean t2Failed = thread2Exception.get() instanceof DataIntegrityViolationException;
		assertThat(t1Failed || t2Failed).isTrue();
		assertThat(t1Failed && t2Failed).isFalse(); // Only one should fail

		// Verify data is consistent: 1 user, 1 merchant request
		var user = appUserRepository.findByEmail("concurrent@test.com");
		assertThat(user).isPresent();
		var merchantReq = merchantRequestRepository.findByUser(user.get());
		assertThat(merchantReq).isPresent();
	}
}
