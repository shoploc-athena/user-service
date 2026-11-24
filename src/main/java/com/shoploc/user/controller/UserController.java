package com.shoploc.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.shoploc.user.dto.AppUserResponse;
import com.shoploc.user.dto.MerchantSignUpRequest;
import com.shoploc.user.service.MerchantService;

@RestController
@RequestMapping("/user")
public class UserController {

	private final MerchantService merchantService;

	public UserController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@PostMapping("/merchant_sign_up")
	public ResponseEntity<AppUserResponse> merchantSignUp(@Valid @RequestBody MerchantSignUpRequest request) {
		AppUserResponse response = merchantService.signUpMerchant(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
