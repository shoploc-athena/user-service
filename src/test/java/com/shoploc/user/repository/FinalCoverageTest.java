package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
import java.time.OffsetDateTime;
import java.util.UUID;
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
class FinalCoverageTest {

	@Autowired
	AppUserRepository appUserRepository;

	@Autowired
	UserPermissionRepository permissionRepository;

	@Autowired
	MerchantRequestRepository merchantRequestRepository;

	@Test
	void shouldCoverAllAppUserFields() {
		AppUser u1 = new AppUser();
		u1.setId(UUID.randomUUID());
		u1.setEmail("final1@test.com");
		u1.setPasswordHash("p1");
		u1.setFirstName("F1");
		u1.setLastName("L1");
		u1.setRole(UserRole.CUSTOMER);
		u1.setStatus(UserStatus.ACTIVE);
		u1.setEmailVerified(true);
		u1.setLicensePlate("LP1");
		OffsetDateTime now = OffsetDateTime.now();
		u1.setCreatedAt(now);
		u1.setVersion(0L);

		assertThat(u1.getId()).isNotNull();
		assertThat(u1.getEmail()).isEqualTo("final1@test.com");
		assertThat(u1.getPasswordHash()).isEqualTo("p1");
		assertThat(u1.getFirstName()).isEqualTo("F1");
		assertThat(u1.getLastName()).isEqualTo("L1");
		assertThat(u1.getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(u1.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(u1.getEmailVerified()).isTrue();
		assertThat(u1.getLicensePlate()).isEqualTo("LP1");
		assertThat(u1.getCreatedAt()).isEqualTo(now);
		assertThat(u1.getVersion()).isEqualTo(0L);
	}

	@Test
	void shouldCoverAllUserPermissionFields() {
		AppUser user = new AppUser();
		user.setEmail("perm.user@test.com");
		user.setPasswordHash("p");
		user.setFirstName("U");
		user.setLastName("P");
		user.setRole(UserRole.MERCHANT);
		user.setStatus(UserStatus.ACTIVE);
		user = appUserRepository.save(user);

		AppUser admin = new AppUser();
		admin.setEmail("perm.admin@test.com");
		admin.setPasswordHash("p");
		admin.setFirstName("A");
		admin.setLastName("P");
		admin.setRole(UserRole.ASSOCIATION);
		admin.setStatus(UserStatus.ACTIVE);
		admin = appUserRepository.save(admin);

		UserPermission p = new UserPermission();
		p.setUser(user);
		p.setGrantedBy(admin);
		p.setPermission(PermissionType.MANAGE_AGENTS);
		UserPermission saved = permissionRepository.save(p);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getUser()).isEqualTo(user);
		assertThat(saved.getGrantedBy()).isEqualTo(admin);
		assertThat(saved.getPermission()).isEqualTo(PermissionType.MANAGE_AGENTS);
	}

	@Test
	void shouldCoverAllMerchantRequestFields() {
		AppUser user = new AppUser();
		user.setEmail("mr.user@test.com");
		user.setPasswordHash("p");
		user.setFirstName("U");
		user.setLastName("M");
		user.setRole(UserRole.MERCHANT);
		user.setStatus(UserStatus.PENDING);
		user = appUserRepository.save(user);

		AppUser reviewer = new AppUser();
		reviewer.setEmail("mr.reviewer@test.com");
		reviewer.setPasswordHash("p");
		reviewer.setFirstName("R");
		reviewer.setLastName("M");
		reviewer.setRole(UserRole.ASSOCIATION);
		reviewer.setStatus(UserStatus.ACTIVE);
		reviewer = appUserRepository.save(reviewer);

		MerchantRequest r = new MerchantRequest();
		r.setUser(user);
		r.setStoreName("Store");
		r.setStoreAddress("Addr");
		r.setStatus(MerchantRequestStatus.PENDING);
		r.setRefusalReason(null);
		r.setReviewedBy(null);
		r.setReviewedAt(null);
		MerchantRequest saved = merchantRequestRepository.save(r);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getUser()).isEqualTo(user);
		assertThat(saved.getStoreName()).isEqualTo("Store");
		assertThat(saved.getStoreAddress()).isEqualTo("Addr");
		assertThat(saved.getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
		assertThat(saved.getRefusalReason()).isNull();
		assertThat(saved.getReviewedBy()).isNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getVersion()).isNotNull();

		saved.setRefusalReason("Reason");
		saved.setReviewedBy(reviewer);
		OffsetDateTime now = OffsetDateTime.now();
		saved.setReviewedAt(now);
		merchantRequestRepository.save(saved);

		MerchantRequest updated = merchantRequestRepository.findById(saved.getId()).orElse(null);
		assertThat(updated).isNotNull();
		assertThat(updated.getRefusalReason()).isEqualTo("Reason");
		assertThat(updated.getReviewedBy()).isEqualTo(reviewer);
		assertThat(updated.getReviewedAt()).isNotNull();
	}
}
