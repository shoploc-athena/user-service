package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class MerchantRequestRepositoryTest {

	@Autowired
	MerchantRequestRepository repository;

	@Autowired
	AppUserRepository appUserRepository;

	private AppUser merchant;
	private AppUser reviewer;

	@BeforeEach
	void setup() {
		repository.deleteAll();
		appUserRepository.deleteAll();

		merchant = new AppUser();
		merchant.setEmail("merchant@example.com");
		merchant.setPasswordHash("pass");
		merchant.setFirstName("Merchant");
		merchant.setLastName("User");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.PENDING);
		merchant = appUserRepository.save(merchant);

		reviewer = new AppUser();
		reviewer.setEmail("reviewer@example.com");
		reviewer.setPasswordHash("pass");
		reviewer.setFirstName("Reviewer");
		reviewer.setLastName("User");
		reviewer.setRole(UserRole.ASSOCIATION);
		reviewer.setStatus(UserStatus.ACTIVE);
		reviewer = appUserRepository.save(reviewer);
	}

	@Test
	void shouldSaveAndRetrieveMerchantRequest() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("My Store");
		request.setStoreAddress("123 Main St");
		request.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest saved = repository.save(request);

		MerchantRequest retrieved = repository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getStoreName()).isEqualTo("My Store");
		assertThat(retrieved.getStoreAddress()).isEqualTo("123 Main St");
		assertThat(retrieved.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
		assertThat(retrieved.getUser().getId()).isEqualTo(merchant.getId());
	}

	@Test
	void shouldUpdateMerchantRequestWithReview() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("My Store");
		request.setStoreAddress("123 Main St");
		request.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest saved = repository.save(request);

		saved.setStatus(MerchantRequestStatus.ACCEPTED);
		saved.setReviewedBy(reviewer);
		saved.setReviewedAt(java.time.OffsetDateTime.now());
		repository.save(saved);

		MerchantRequest updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getStatus()).isEqualTo(MerchantRequestStatus.ACCEPTED);
		assertThat(updated.getReviewedBy().getId()).isEqualTo(reviewer.getId());
		assertThat(updated.getReviewedAt()).isNotNull();
	}

	@Test
	void shouldRefuseMerchantRequestWithReason() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("My Store");
		request.setStoreAddress("123 Main St");
		request.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest saved = repository.save(request);

		saved.setStatus(MerchantRequestStatus.REFUSED);
		saved.setRefusalReason("Invalid documentation");
		saved.setReviewedBy(reviewer);
		repository.save(saved);

		MerchantRequest updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getStatus()).isEqualTo(MerchantRequestStatus.REFUSED);
		assertThat(updated.getRefusalReason()).isEqualTo("Invalid documentation");
	}

	@Test
	void shouldListMerchantRequests() {
		MerchantRequest req1 = new MerchantRequest();
		req1.setUser(merchant);
		req1.setStoreName("Store 1");
		req1.setStoreAddress("123 Main St");
		req1.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest req2 = new MerchantRequest();
		req2.setUser(merchant);
		req2.setStoreName("Store 2");
		req2.setStoreAddress("456 Oak Ave");
		req2.setStatus(MerchantRequestStatus.PENDING);

		repository.save(req1);
		repository.save(req2);

		var allRequests = repository.findAll();
		assertThat(allRequests).hasSize(2);
	}
}
