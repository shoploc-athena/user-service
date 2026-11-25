package com.shoploc.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import com.shoploc.user.dto.MerchantSignUpResponse;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.exception.ApiExceptionHandler;
import com.shoploc.user.exception.EmailAlreadyExistsException;
import com.shoploc.user.service.MerchantService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private MerchantService merchantService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new UserController(merchantService))
				.setControllerAdvice(new ApiExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void shouldSuccessfullyRegisterMerchant() throws Exception {
		UUID requestId = UUID.randomUUID();
		when(merchantService.signUpMerchant(any()))
			.thenReturn(new MerchantSignUpResponse(requestId, MerchantRequestStatus.PENDING));

		mockMvc.perform(post("/api/users/merchants")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"merchant@example.com","password":"securepassword123",
								"firstName":"Jane","lastName":"Doe","storeName":"Store",
								"storeAddress":"1 Main Street"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.requestId").value(requestId.toString()))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void shouldRejectInvalidEmail() throws Exception {
		mockMvc.perform(post("/api/users/merchants")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid","password":"securepassword123","firstName":"Jane","lastName":"Doe",
								"storeName":"Store","storeAddress":"1 Main Street"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(merchantService);
	}

	@Test
	void shouldRejectWeakPassword() throws Exception {
		mockMvc.perform(post("/api/users/merchants")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"merchant@example.com","password":"weak",
								"firstName":"Jane","lastName":"Doe","storeName":"Store",
								"storeAddress":"1 Main Street"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(merchantService);
	}

	@Test
	void shouldRejectPasswordTooLong() throws Exception {
		String longPassword = "a".repeat(73);
		mockMvc.perform(post("/api/users/merchants")
						.contentType(MediaType.APPLICATION_JSON)
						.content(String.format("""
								{"email":"merchant@example.com","password":"%s",
								"firstName":"Jane","lastName":"Doe","storeName":"Store",
								"storeAddress":"1 Main Street"}
								""", longPassword)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(merchantService);
	}

	@Test
	void shouldReturnConflictForExistingEmail() throws Exception {
		when(merchantService.signUpMerchant(any())).thenThrow(new EmailAlreadyExistsException());

		mockMvc.perform(post("/api/users/merchants")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"merchant@example.com","password":"securepassword123",
								"firstName":"Jane","lastName":"Doe","storeName":"Store",
								"storeAddress":"1 Main Street"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void shouldRejectMissingFields() throws Exception {
		mockMvc.perform(post("/api/users/merchants")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"merchant@example.com","password":"securepassword123"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errors").isArray());

		verifyNoInteractions(merchantService);
	}
}
