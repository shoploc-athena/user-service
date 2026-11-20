package com.shoploc.user.entity;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

class EntityEnumsTest {

	@Test
	void shouldHaveAllUserRoles() {
		assertThat(UserRole.values()).contains(
			UserRole.CUSTOMER,
			UserRole.MERCHANT,
			UserRole.ASSOCIATION,
			UserRole.TOWN_HALL
		);
		assertThat(UserRole.values()).hasSize(4);
	}

	@Test
	void shouldHaveAllUserStatuses() {
		assertThat(UserStatus.values()).contains(
			UserStatus.PENDING,
			UserStatus.ACTIVE,
			UserStatus.DISABLED
		);
		assertThat(UserStatus.values()).hasSize(3);
	}

	@Test
	void shouldHaveAllMerchantRequestStatuses() {
		assertThat(MerchantRequestStatus.values()).contains(
			MerchantRequestStatus.PENDING,
			MerchantRequestStatus.ACCEPTED,
			MerchantRequestStatus.REFUSED
		);
		assertThat(MerchantRequestStatus.values()).hasSize(3);
	}

	@Test
	void shouldHaveAllPermissionTypes() {
		assertThat(PermissionType.values()).contains(
			PermissionType.MANAGE_AGENTS,
			PermissionType.VALIDATE_MERCHANTS,
			PermissionType.CONFIGURE_PROGRAM,
			PermissionType.CONFIGURE_BENEFITS,
			PermissionType.VIEW_INDICATORS,
			PermissionType.EXPORT_INDICATORS,
			PermissionType.PUBLISH_ANNOUNCEMENTS
		);
		assertThat(PermissionType.values()).hasSize(7);
	}

	@Test
	void shouldParseUserRoleFromString() {
		assertThat(UserRole.valueOf("CUSTOMER")).isEqualTo(UserRole.CUSTOMER);
		assertThat(UserRole.valueOf("MERCHANT")).isEqualTo(UserRole.MERCHANT);
	}

	@Test
	void shouldParseStatusFromString() {
		assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
		assertThat(UserStatus.valueOf("PENDING")).isEqualTo(UserStatus.PENDING);
	}
}
