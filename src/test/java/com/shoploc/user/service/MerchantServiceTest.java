package com.shoploc.user.service;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.dto.AppUserResponse;
import com.shoploc.user.dto.MerchantSignUpRequest;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
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
			"password123",
			"John",
			"Doe",
			"My Store",
			"123 Main Street"
		);

		AppUserResponse response = merchantService.signUpMerchant(request);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isNotNull();
		assertThat(response.getEmail()).isEqualTo("merchant@test.com");
		assertThat(response.getFirstName()).isEqualTo("John");
		assertThat(response.getLastName()).isEqualTo("Doe");
		assertThat(response.getRole()).isEqualTo(UserRole.MERCHANT);
		assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING);

		// Vérifier en base
		var user = appUserRepository.findByEmail("merchant@test.com");
		assertThat(user).isPresent();
		assertThat(user.get().getEmailVerified()).isFalse();

		// Vérifier la demande d'adhésion
		var requests = merchantRequestRepository.findAll();
		assertThat(requests).anyMatch(r -> r.getUser().getId().equals(user.get().getId())
			&& r.getStoreName().equals("My Store"));
	}

	@Test
	void shouldRejectDuplicateEmail() {
		MerchantSignUpRequest request1 = new MerchantSignUpRequest(
			"duplicate@test.com",
			"password1",
			"First",
			"User",
			"Store 1",
			"Address 1"
		);

		merchantService.signUpMerchant(request1);

		MerchantSignUpRequest request2 = new MerchantSignUpRequest(
			"duplicate@test.com",
			"password2",
			"Second",
			"User",
			"Store 2",
			"Address 2"
		);

		assertThatThrownBy(() -> merchantService.signUpMerchant(request2))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Email already exists");
	}

	@Test
	void shouldCreateMerchantWithPendingStatus() {
		MerchantSignUpRequest request = new MerchantSignUpRequest(
			"newmerchant@test.com",
			"securepass",
			"Jane",
			"Smith",
			"Boutique",
			"456 Oak Avenue"
		);

		AppUserResponse response = merchantService.signUpMerchant(request);

		assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING);

		var user = appUserRepository.findById(response.getId()).get();
		assertThat(user.getRole()).isEqualTo(UserRole.MERCHANT);
		assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
		assertThat(user.getEmailVerified()).isFalse();

		var merchantReq = merchantRequestRepository.findAll().stream()
			.filter(r -> r.getUser().getId().equals(user.getId()))
			.findFirst();
		assertThat(merchantReq).isPresent();
		assertThat(merchantReq.get().getStoreName()).isEqualTo("Boutique");
	}
}
