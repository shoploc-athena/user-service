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
import com.shoploc.user.entity.PermissionType;
import com.shoploc.user.entity.UserPermission;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Transactional
@Import(TestcontainersConfiguration.class)
class CoverageBoostTest {

	@Autowired
	AppUserRepository appUserRepository;

	@Autowired
	UserPermissionRepository permissionRepository;

	@Autowired
	MerchantRequestRepository merchantRequestRepository;

	private AppUser user1;
	private AppUser user2;
	private AppUser user3;

	@BeforeEach
	void setup() {
		user1 = new AppUser();
		user1.setEmail("user1@test.com");
		user1.setPasswordHash("pass1");
		user1.setFirstName("User");
		user1.setLastName("One");
		user1.setRole(UserRole.CUSTOMER);
		user1.setStatus(UserStatus.ACTIVE);
		user1 = appUserRepository.save(user1);

		user2 = new AppUser();
		user2.setEmail("user2@test.com");
		user2.setPasswordHash("pass2");
		user2.setFirstName("User");
		user2.setLastName("Two");
		user2.setRole(UserRole.MERCHANT);
		user2.setStatus(UserStatus.PENDING);
		user2 = appUserRepository.save(user2);

		user3 = new AppUser();
		user3.setEmail("user3@test.com");
		user3.setPasswordHash("pass3");
		user3.setFirstName("User");
		user3.setLastName("Three");
		user3.setRole(UserRole.ASSOCIATION);
		user3.setStatus(UserStatus.ACTIVE);
		user3 = appUserRepository.save(user3);
	}

	@Test
	void shouldTestAllUserStatusTransitions() {
		AppUser user = new AppUser();
		user.setEmail("status.test@test.com");
		user.setPasswordHash("pass");
		user.setFirstName("Status");
		user.setLastName("Test");
		user.setRole(UserRole.MERCHANT);

		for (UserStatus status : UserStatus.values()) {
			user.setStatus(status);
			AppUser saved = appUserRepository.save(user);
			assertThat(saved.getStatus()).isEqualTo(status);
		}
	}

	@Test
	void shouldTestAllMerchantRequestStatusTransitions() {
		MerchantRequest req = new MerchantRequest();
		req.setUser(user2);
		req.setStoreName("Multi-Status Store");
		req.setStoreAddress("Test Address");

		for (MerchantRequestStatus status : MerchantRequestStatus.values()) {
			req.setStatus(status);
			if (status != MerchantRequestStatus.PENDING) {
				req.setReviewedBy(user3);
				req.setReviewedAt(OffsetDateTime.now());
			}
			MerchantRequest saved = merchantRequestRepository.save(req);
			assertThat(saved.getStatus()).isEqualTo(status);
		}
	}

	@Test
	void shouldTestPermissionWithEachPermissionType() {
		for (PermissionType type : PermissionType.values()) {
			UserPermission perm = new UserPermission();
			perm.setUser(user2);
			perm.setGrantedBy(user3);
			perm.setPermission(type);
			UserPermission saved = permissionRepository.save(perm);
			assertThat(saved.getPermission()).isEqualTo(type);
		}
	}

	@Test
	void shouldTestCompleteCrudOperations() {
		// CREATE
		AppUser newUser = new AppUser();
		newUser.setEmail("crud@test.com");
		newUser.setPasswordHash("pass");
		newUser.setFirstName("CRUD");
		newUser.setLastName("Test");
		newUser.setRole(UserRole.TOWN_HALL);
		newUser.setStatus(UserStatus.ACTIVE);
		newUser.setEmailVerified(true);
		newUser.setLicensePlate("CRUD-001");

		AppUser created = appUserRepository.save(newUser);
		assertThat(created.getId()).isNotNull();

		// READ
		AppUser read = appUserRepository.findById(created.getId()).orElse(null);
		assertThat(read).isNotNull();
		assertThat(read.getEmail()).isEqualTo("crud@test.com");

		// UPDATE
		read.setFirstName("UpdatedCRUD");
		read.setLastName("UpdatedTest");
		read.setEmailVerified(false);
		AppUser updated = appUserRepository.save(read);
		assertThat(updated.getFirstName()).isEqualTo("UpdatedCRUD");

		// VERIFY UPDATE
		AppUser verified = appUserRepository.findById(updated.getId()).orElse(null);
		assertThat(verified.getLastName()).isEqualTo("UpdatedTest");
		assertThat(verified.getEmailVerified()).isFalse();
	}

	@Test
	void shouldTestMerchantRequestCompleteLifecycle() {
		MerchantRequest req = new MerchantRequest(user2, "Complete Lifecycle Store", "123 Complete St");
		MerchantRequest saved = merchantRequestRepository.save(req);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getVersion()).isNotNull();
		assertThat(saved.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);

		saved.setStatus(MerchantRequestStatus.ACCEPTED);
		saved.setReviewedBy(user3);
		saved.setReviewedAt(OffsetDateTime.now());
		merchantRequestRepository.save(saved);

		MerchantRequest updated = merchantRequestRepository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getStatus()).isEqualTo(MerchantRequestStatus.ACCEPTED);
		assertThat(updated.getReviewedAt()).isNotNull();
	}
}
