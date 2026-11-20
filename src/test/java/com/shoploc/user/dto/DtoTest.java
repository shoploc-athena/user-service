package com.shoploc.user.dto;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import java.util.UUID;

class DtoTest {

	@Test
	void shouldTestMerchantSignUpRequestGettersSetters() {
		MerchantSignUpRequest request = new MerchantSignUpRequest();
		request.setEmail("merchant@test.com");
		request.setPassword("pass123");
		request.setFirstName("John");
		request.setLastName("Doe");
		request.setStoreName("MyStore");
		request.setStoreAddress("123 Main St");

		assertThat(request.getEmail()).isEqualTo("merchant@test.com");
		assertThat(request.getPassword()).isEqualTo("pass123");
		assertThat(request.getFirstName()).isEqualTo("John");
		assertThat(request.getLastName()).isEqualTo("Doe");
		assertThat(request.getStoreName()).isEqualTo("MyStore");
		assertThat(request.getStoreAddress()).isEqualTo("123 Main St");
	}

	@Test
	void shouldTestMerchantSignUpRequestConstructor() {
		MerchantSignUpRequest request = new MerchantSignUpRequest(
			"merchant@test.com", "pass123", "John", "Doe", "MyStore", "123 Main St"
		);

		assertThat(request.getEmail()).isEqualTo("merchant@test.com");
		assertThat(request.getPassword()).isEqualTo("pass123");
		assertThat(request.getFirstName()).isEqualTo("John");
		assertThat(request.getLastName()).isEqualTo("Doe");
		assertThat(request.getStoreName()).isEqualTo("MyStore");
		assertThat(request.getStoreAddress()).isEqualTo("123 Main St");
	}

	@Test
	void shouldTestAppUserResponseGettersSetters() {
		UUID userId = UUID.randomUUID();
		AppUserResponse response = new AppUserResponse();
		response.setId(userId);
		response.setEmail("user@test.com");
		response.setFirstName("Jane");
		response.setLastName("Smith");
		response.setRole(UserRole.CUSTOMER);
		response.setStatus(UserStatus.ACTIVE);

		assertThat(response.getId()).isEqualTo(userId);
		assertThat(response.getEmail()).isEqualTo("user@test.com");
		assertThat(response.getFirstName()).isEqualTo("Jane");
		assertThat(response.getLastName()).isEqualTo("Smith");
		assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	void shouldTestAppUserResponseConstructor() {
		UUID userId = UUID.randomUUID();
		AppUserResponse response = new AppUserResponse(
			userId, "user@test.com", "Jane", "Smith", UserRole.CUSTOMER, UserStatus.ACTIVE
		);

		assertThat(response.getId()).isEqualTo(userId);
		assertThat(response.getEmail()).isEqualTo("user@test.com");
		assertThat(response.getFirstName()).isEqualTo("Jane");
		assertThat(response.getLastName()).isEqualTo("Smith");
		assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);
		assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
	}
}
