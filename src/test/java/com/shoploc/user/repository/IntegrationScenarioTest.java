package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.PermissionType;
import com.shoploc.user.entity.UserPermission;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class IntegrationScenarioTest {

	@Autowired
	AppUserRepository appUserRepository;

	@Autowired
	UserPermissionRepository permissionRepository;

	@Autowired
	MerchantRequestRepository merchantRequestRepository;

	@Test
	void shouldHandleCompleteMerchantOnboardingWorkflow() {
		// 1. Create merchant user
		AppUser merchant = new AppUser();
		merchant.setEmail("newmerchant@store.com");
		merchant.setPasswordHash("secure_hash");
		merchant.setFirstName("Jane");
		merchant.setLastName("Merchant");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.PENDING);
		merchant.setEmailVerified(false);
		merchant.setLicensePlate("MERCHANT-001");

		AppUser savedMerchant = appUserRepository.save(merchant);
		assertThat(savedMerchant.getId()).isNotNull();
		assertThat(savedMerchant.getCreatedAt()).isNotNull();
		assertThat(savedMerchant.getVersion()).isNotNull();

		// 2. Create admin user who will review
		AppUser admin = new AppUser();
		admin.setEmail("admin@association.com");
		admin.setPasswordHash("admin_hash");
		admin.setFirstName("Admin");
		admin.setLastName("User");
		admin.setRole(UserRole.ASSOCIATION);
		admin.setStatus(UserStatus.ACTIVE);
		admin.setEmailVerified(true);

		AppUser savedAdmin = appUserRepository.save(admin);

		// 3. Create merchant request
		MerchantRequest request = new MerchantRequest(savedMerchant, "My Coffee Shop", "123 Main Street");
		assertThat(request.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);

		MerchantRequest savedRequest = merchantRequestRepository.save(request);
		assertThat(savedRequest.getId()).isNotNull();
		assertThat(savedRequest.getCreatedAt()).isNotNull();

		// 4. Admin reviews and accepts
		savedRequest.setStatus(MerchantRequestStatus.ACCEPTED);
		savedRequest.setReviewedBy(savedAdmin);
		savedRequest.setReviewedAt(OffsetDateTime.now());
		merchantRequestRepository.save(savedRequest);

		MerchantRequest acceptedRequest = merchantRequestRepository.findById(savedRequest.getId()).orElse(null);
		assertThat(acceptedRequest).isNotNull();
		assertThat(acceptedRequest.getStatus()).isEqualTo(MerchantRequestStatus.ACCEPTED);
		assertThat(acceptedRequest.getReviewedBy()).isNotNull();

		// 5. Grant permissions to merchant
		UserPermission perm = new UserPermission();
		perm.setUser(savedMerchant);
		perm.setGrantedBy(savedAdmin);
		perm.setPermission(PermissionType.CONFIGURE_PROGRAM);

		UserPermission savedPerm = permissionRepository.save(perm);
		assertThat(savedPerm.getId()).isNotNull();

		// 6. Update merchant status to ACTIVE
		savedMerchant.setStatus(UserStatus.ACTIVE);
		savedMerchant.setEmailVerified(true);
		AppUser activeMerchant = appUserRepository.save(savedMerchant);

		// 7. Verify complete workflow
		AppUser finalMerchant = appUserRepository.findById(activeMerchant.getId()).orElse(null);
		assertThat(finalMerchant).isNotNull();
		assertThat(finalMerchant.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(finalMerchant.getEmailVerified()).isTrue();

		MerchantRequest finalRequest = merchantRequestRepository.findById(savedRequest.getId()).orElse(null);
		assertThat(finalRequest).isNotNull();
		assertThat(finalRequest.getStatus()).isEqualTo(MerchantRequestStatus.ACCEPTED);

		var permissions = permissionRepository.findAll();
		assertThat(permissions).anyMatch(p -> p.getUser().getId().equals(finalMerchant.getId()));
	}

	@Test
	void shouldHandleMerchantRejectionWorkflow() {
		AppUser merchant = new AppUser();
		merchant.setEmail("rejected@store.com");
		merchant.setPasswordHash("hash");
		merchant.setFirstName("Bob");
		merchant.setLastName("Failed");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.PENDING);

		AppUser admin = new AppUser();
		admin.setEmail("reviewer@assoc.com");
		admin.setPasswordHash("hash");
		admin.setFirstName("Reviewer");
		admin.setLastName("Admin");
		admin.setRole(UserRole.ASSOCIATION);
		admin.setStatus(UserStatus.ACTIVE);

		merchant = appUserRepository.save(merchant);
		admin = appUserRepository.save(admin);

		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("Invalid Store");
		request.setStoreAddress("Invalid Address");
		request.setStatus(MerchantRequestStatus.PENDING);

		request = merchantRequestRepository.save(request);

		request.setStatus(MerchantRequestStatus.REFUSED);
		request.setRefusalReason("Missing required documentation and business license");
		request.setReviewedBy(admin);
		request.setReviewedAt(OffsetDateTime.now());
		merchantRequestRepository.save(request);

		MerchantRequest finalRequest = merchantRequestRepository.findById(request.getId()).orElse(null);
		assertThat(finalRequest).isNotNull();
		assertThat(finalRequest.getStatus()).isEqualTo(MerchantRequestStatus.REFUSED);
		assertThat(finalRequest.getRefusalReason()).isNotNull();
		assertThat(finalRequest.getRefusalReason()).contains("documentation");
	}
}
