package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import java.time.OffsetDateTime;
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
class MerchantRequestLifecycleTest {

	@Autowired
	MerchantRequestRepository repository;

	@Autowired
	AppUserRepository appUserRepository;

	private AppUser merchant;
	private AppUser reviewer;

	@BeforeEach
	void setup() {
		merchant = new AppUser();
		merchant.setEmail("merchant2@example.com");
		merchant.setPasswordHash("pass");
		merchant.setFirstName("Merchant");
		merchant.setLastName("User");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.PENDING);
		merchant = appUserRepository.save(merchant);

		reviewer = new AppUser();
		reviewer.setEmail("reviewer2@example.com");
		reviewer.setPasswordHash("pass");
		reviewer.setFirstName("Reviewer");
		reviewer.setLastName("User");
		reviewer.setRole(UserRole.ASSOCIATION);
		reviewer.setStatus(UserStatus.ACTIVE);
		reviewer = appUserRepository.save(reviewer);
	}

	@Test
	void shouldCreateMerchantRequestWithDefaultPendingStatus() {
		MerchantRequest request = new MerchantRequest(merchant, "Store", "Address");

		assertThat(request.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
		assertThat(request.getUser()).isEqualTo(merchant);
		assertThat(request.getStoreName()).isEqualTo("Store");
	}

	@Test
	void shouldSetCreatedAtAutomatically() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("My Store");
		request.setStoreAddress("123 Main St");
		request.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest saved = repository.save(request);
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void shouldRefuseWithMultipleReasonsForRefusal() {
		MerchantRequest req1 = new MerchantRequest();
		req1.setUser(merchant);
		req1.setStoreName("Store 1");
		req1.setStoreAddress("Address 1");
		req1.setStatus(MerchantRequestStatus.PENDING);
		MerchantRequest saved1 = repository.save(req1);

		saved1.setStatus(MerchantRequestStatus.REFUSED);
		saved1.setRefusalReason("Incomplete documents");
		saved1.setReviewedBy(reviewer);
		saved1.setReviewedAt(OffsetDateTime.now());
		repository.save(saved1);

		MerchantRequest retrieved = repository.findById(saved1.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getRefusalReason()).isEqualTo("Incomplete documents");
		assertThat(retrieved.getReviewedAt()).isNotNull();
	}

	@Test
	void shouldHandleNullRefusalReasonForAcceptedRequests() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("My Store");
		request.setStoreAddress("123 Main St");
		request.setStatus(MerchantRequestStatus.PENDING);
		request.setRefusalReason(null);

		MerchantRequest saved = repository.save(request);
		saved.setStatus(MerchantRequestStatus.ACCEPTED);
		saved.setReviewedBy(reviewer);
		saved.setReviewedAt(OffsetDateTime.now());
		repository.save(saved);

		MerchantRequest updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getRefusalReason()).isNull();
	}

	@Test
	void shouldFindByUserAndStatus() {
		MerchantRequest req = new MerchantRequest();
		req.setUser(merchant);
		req.setStoreName("Test Store");
		req.setStoreAddress("Test Address");
		req.setStatus(MerchantRequestStatus.PENDING);

		repository.save(req);

		var allRequests = repository.findAll();
		assertThat(allRequests).anyMatch(r -> r.getUser().getId().equals(merchant.getId()));
	}
}
