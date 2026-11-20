package com.shoploc.user.repository;

import static org.assertj.core.api.Assertions.*;
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
class EdgeCasesTest {

	@Autowired
	AppUserRepository appUserRepository;

	@Autowired
	UserPermissionRepository permissionRepository;

	@Autowired
	MerchantRequestRepository merchantRequestRepository;

	@Test
	void shouldHandleVeryLongStrings() {
		AppUser user = new AppUser();
		user.setEmail("long.email.with.many.dots@very.long.domain.example.com");
		user.setPasswordHash("very_long_password_hash_with_many_characters_1234567890_abcdefghijklmnop");
		user.setFirstName("VeryLongFirstNameWithManyCharactersToTest");
		user.setLastName("VeryLongLastNameWithManyCharactersToTest");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);
		user.setLicensePlate("LONGLICENSE-12345");

		AppUser saved = appUserRepository.save(user);

		AppUser retrieved = appUserRepository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getEmail()).contains("long.email");
		assertThat(retrieved.getLicensePlate()).contains("LONG");
	}

	@Test
	void shouldHandleMultiplePermissionTypes() {
		AppUser user = new AppUser();
		user.setEmail("multiperms@test.com");
		user.setPasswordHash("pass");
		user.setFirstName("Multi");
		user.setLastName("Perm");
		user.setRole(UserRole.MERCHANT);
		user.setStatus(UserStatus.ACTIVE);
		user = appUserRepository.save(user);

		AppUser admin = new AppUser();
		admin.setEmail("admin.perms@test.com");
		admin.setPasswordHash("pass");
		admin.setFirstName("Admin");
		admin.setLastName("Perms");
		admin.setRole(UserRole.ASSOCIATION);
		admin.setStatus(UserStatus.ACTIVE);
		admin = appUserRepository.save(admin);

		// Grant all permission types
		for (PermissionType permType : new PermissionType[]{
			PermissionType.MANAGE_AGENTS,
			PermissionType.VALIDATE_MERCHANTS,
			PermissionType.CONFIGURE_PROGRAM,
			PermissionType.CONFIGURE_BENEFITS,
			PermissionType.VIEW_INDICATORS,
			PermissionType.EXPORT_INDICATORS,
			PermissionType.PUBLISH_ANNOUNCEMENTS
		}) {
			UserPermission perm = new UserPermission();
			perm.setUser(user);
			perm.setGrantedBy(admin);
			perm.setPermission(permType);
			permissionRepository.save(perm);
		}

		var perms = permissionRepository.findAll();
		assertThat(perms).hasSizeGreaterThanOrEqualTo(7);
	}

	@Test
	void shouldHandleSpecialCharactersInText() {
		AppUser merchant = new AppUser();
		merchant.setEmail("special@test.com");
		merchant.setPasswordHash("hash");
		merchant.setFirstName("José");
		merchant.setLastName("O'Brien");
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.ACTIVE);
		merchant = appUserRepository.save(merchant);

		MerchantRequest request = new MerchantRequest();
		request.setUser(merchant);
		request.setStoreName("Café & Bar \"O'Mally's\"");
		request.setStoreAddress("123 Rue de l'Église, Québec");
		request.setStatus(MerchantRequestStatus.PENDING);

		MerchantRequest saved = merchantRequestRepository.save(request);

		MerchantRequest retrieved = merchantRequestRepository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getStoreName()).contains("Café");
		assertThat(retrieved.getStoreAddress()).contains("Église");
	}

	@Test
	void shouldHandleNullableFields() {
		AppUser user = new AppUser();
		user.setEmail("nullable@test.com");
		user.setPasswordHash("hash");
		user.setFirstName("Test");
		user.setLastName("Null");
		user.setRole(UserRole.CUSTOMER);
		user.setStatus(UserStatus.ACTIVE);
		user.setEmailVerified(true);
		user.setLicensePlate(null);

		AppUser saved = appUserRepository.save(user);

		AppUser retrieved = appUserRepository.findById(saved.getId()).orElse(null);
		assertThat(retrieved).isNotNull();
		assertThat(retrieved.getLicensePlate()).isNull();
	}
}
