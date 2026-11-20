package com.shoploc.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shoploc.user.dto.AppUserResponse;
import com.shoploc.user.dto.MerchantSignUpRequest;
import com.shoploc.user.service.MerchantService;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private MerchantService merchantService;

	@PostMapping("/merchant_sign_up")
	public ResponseEntity<AppUserResponse> merchantSignUp(@RequestBody MerchantSignUpRequest request) {
		AppUserResponse response = merchantService.signUpMerchant(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
