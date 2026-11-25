package com.shoploc.user.service;

import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.dto.MerchantSignUpRequest;
import com.shoploc.user.dto.MerchantSignUpResponse;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.exception.EmailAlreadyExistsException;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.MerchantRequestRepository;

@Service
public class MerchantService {

	private final AppUserRepository appUserRepository;
	private final MerchantRequestRepository merchantRequestRepository;
	private final PasswordEncoder passwordEncoder;

	public MerchantService(AppUserRepository appUserRepository,
						   MerchantRequestRepository merchantRequestRepository,
						   PasswordEncoder passwordEncoder) {
		this.appUserRepository = appUserRepository;
		this.merchantRequestRepository = merchantRequestRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public MerchantSignUpResponse signUpMerchant(MerchantSignUpRequest request) {
		String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

		Optional<AppUser> existing = appUserRepository.findByEmail(normalizedEmail);
		if (existing.isPresent()) {
			throw new EmailAlreadyExistsException();
		}

		AppUser merchant = new AppUser();
		merchant.setEmail(normalizedEmail);
		merchant.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		merchant.setFirstName(request.getFirstName());
		merchant.setLastName(request.getLastName());
		merchant.setRole(UserRole.MERCHANT);
		merchant.setStatus(UserStatus.PENDING);
		merchant.setEmailVerified(false);

		AppUser savedMerchant = appUserRepository.save(merchant);

		MerchantRequest merchantRequest = new MerchantRequest();
		merchantRequest.setUser(savedMerchant);
		merchantRequest.setStoreName(request.getStoreName());
		merchantRequest.setStoreAddress(request.getStoreAddress());
		merchantRequest.setStatus(MerchantRequestStatus.PENDING);
		MerchantRequest savedRequest = merchantRequestRepository.save(merchantRequest);

		return new MerchantSignUpResponse(savedRequest.getId(), savedRequest.getStatus());
	}
}