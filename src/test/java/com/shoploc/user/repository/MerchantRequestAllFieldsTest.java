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
class MerchantRequestAllFieldsTest {

	@Autowired
	MerchantRequestRepository repository;

	@Autowired
	AppUserRepository appUserRepository;

	private AppUser merchant;
	private AppUser reviewer;

	@BeforeEach
	void setup() {
		merchant = new AppUser();
		merchant.setEmail("merchant4@example.com");
		merchant.setPasswordHash("pass");
		merchant.setFirstName("Merchant");
		merchant.setLastName("User");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.PENDING);
		merchant = appUserRepository.save(merchant);

		reviewer = new AppUser();
		reviewer.setEmail("reviewer4@example.com");
		reviewer.setPasswordHash("pass");
		reviewer.setFirstName("Reviewer");
		reviewer.setLastName("User");
		reviewer.setRole(UserRole.ASSOCIATION);
		reviewer.setStatus(UserStatus.ACTIVE);
		reviewer = appUserRepository.save(reviewer);
	}

	@Test
	void shouldSetAndGetAllFields() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("Complete Store");
		request.setStoreAddress("100 Complete St");
		request.setStatus(MerchantRequestStatus.PENDING);
		request.setRefusalReason(null);
		request.setReviewedBy(null);
		request.setReviewedAt(null);

		MerchantRequest saved = repository.save(request);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getUser()).isEqualTo(merchant);
		assertThat(saved.getStoreName()).isEqualTo("Complete Store");
		assertThat(saved.getStoreAddress()).isEqualTo("100 Complete St");
		assertThat(saved.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
		assertThat(saved.getRefusalReason()).isNull();
		assertThat(saved.getReviewedBy()).isNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getVersion()).isNotNull();
	}

	@Test
	void shouldTestAllMerchantRequestStatuses() {
		int index = 0;
		for (MerchantRequestStatus status : MerchantRequestStatus.values()) {
			MerchantRequest request = new MerchantRequest();
			request.setUser(merchant);
			request.setStoreName("Store " + index);
			request.setStoreAddress("Address " + index);
			request.setStatus(status);

			if (status == MerchantRequestStatus.REFUSED) {
				request.setRefusalReason("Reason");
			}
			if (status != MerchantRequestStatus.PENDING) {
				request.setReviewedBy(reviewer);
				request.setReviewedAt(OffsetDateTime.now());
			}

			MerchantRequest saved = repository.save(request);

			MerchantRequest retrieved = repository.findById(saved.getId()).orElse(null);
			assertThat(retrieved).isNotNull();
			assertThat(retrieved.getStatus()).isEqualTo(status);
			index++;
		}
	}

	@Test
	void shouldUpdateCompleteRequest() {
		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("Original");
		request.setStoreAddress("Original Address");
		request.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest saved = repository.save(request);

		saved.setStoreName("Updated");
		saved.setStoreAddress("Updated Address");
		saved.setStatus(MerchantRequestStatus.ACCEPTED);
		saved.setReviewedBy(reviewer);
		saved.setReviewedAt(OffsetDateTime.now());
		repository.save(saved);

		MerchantRequest updated = repository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getStoreName()).isEqualTo("Updated");
		assertThat(updated.getStoreAddress()).isEqualTo("Updated Address");
		assertThat(updated.getStatus()).isEqualTo(MerchantRequestStatus.ACCEPTED);
		assertThat(updated.getReviewedBy()).isEqualTo(reviewer);
	}
}
