package com.shoploc.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.shoploc.user.dto.MerchantSignUpRequest;
import com.shoploc.user.dto.MerchantSignUpResponse;
import com.shoploc.user.service.MerchantService;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User registration and management endpoints")
public class UserController {

	private final MerchantService merchantService;

	public UserController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@PostMapping("/merchants")
	@Operation(summary = "Register a new merchant", description = "Create a new merchant account and pending merchant request. Email is normalized (trimmed and lowercased).")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Merchant registered successfully", content = @Content(schema = @Schema(implementation = MerchantSignUpResponse.class))),
		@ApiResponse(responseCode = "400", description = "Validation error (invalid email, weak password, etc.)"),
		@ApiResponse(responseCode = "409", description = "Email already exists")
	})
	public ResponseEntity<MerchantSignUpResponse> signUpMerchant(@Valid @RequestBody MerchantSignUpRequest request) {
		MerchantSignUpResponse response = merchantService.signUpMerchant(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
