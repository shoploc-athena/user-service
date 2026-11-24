package com.shoploc.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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
	void shouldRejectInvalidSignupRequest() throws Exception {
		mockMvc.perform(post("/user/merchant_sign_up")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"invalid","password":"","firstName":"","lastName":"",
								"storeName":"","storeAddress":""}
								"""))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(merchantService);
	}

	@Test
	void shouldReturnConflictForExistingEmail() throws Exception {
		when(merchantService.signUpMerchant(any())).thenThrow(new EmailAlreadyExistsException());

		mockMvc.perform(post("/user/merchant_sign_up")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"merchant@example.com","password":"password",
								"firstName":"Jane","lastName":"Doe","storeName":"Store",
								"storeAddress":"1 Main Street"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
	}
}
