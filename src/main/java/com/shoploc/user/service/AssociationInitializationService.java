package com.shoploc.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.PermissionType;
import com.shoploc.user.entity.UserPermission;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.UserPermissionRepository;

@Service
public class AssociationInitializationService {
	private static final Logger logger = LoggerFactory.getLogger(AssociationInitializationService.class);
	private static final int MIN_PASSWORD_LENGTH = 12;

	private final AppUserRepository appUserRepository;
	private final UserPermissionRepository userPermissionRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${ASSOCIATION_ADMIN_EMAIL:}")
	private String associationAdminEmail;

	@Value("${ASSOCIATION_ADMIN_PASSWORD:}")
	private String associationAdminPassword;

	public AssociationInitializationService(AppUserRepository appUserRepository,
											UserPermissionRepository userPermissionRepository,
											PasswordEncoder passwordEncoder) {
		this.appUserRepository = appUserRepository;
		this.userPermissionRepository = userPermissionRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void initializeFirstAssociationAccount() {
		if (associationAdminEmail == null || associationAdminEmail.isBlank() ||
			associationAdminPassword == null || associationAdminPassword.isBlank()) {
			logger.error("Association admin credentials not provided in environment variables");
			return;
		}

		if (associationAdminPassword.length() < MIN_PASSWORD_LENGTH) {
			logger.error("Association admin password must be at least {} characters long", MIN_PASSWORD_LENGTH);
			return;
		}

		if (appUserRepository.existsByRole(UserRole.ASSOCIATION)) {
			logger.debug("Association account already exists");
			return;
		}

		if (appUserRepository.findByEmail(associationAdminEmail).isPresent()) {
			logger.debug("Email already exists, skipping association account creation");
			return;
		}

		AppUser associationAdmin = new AppUser();
		associationAdmin.setEmail(associationAdminEmail);
		associationAdmin.setPasswordHash(passwordEncoder.encode(associationAdminPassword));
		associationAdmin.setFirstName("Association");
		associationAdmin.setLastName("Admin");
		associationAdmin.setRole(UserRole.ASSOCIATION);
		associationAdmin.setStatus(UserStatus.ACTIVE);
		associationAdmin.setEmailVerified(true);

		AppUser savedUser = appUserRepository.saveAndFlush(associationAdmin);
		logger.info("Association account created with ID: {}", savedUser.getId());

		createPermissions(savedUser);
	}

	private void createPermissions(AppUser user) {
		UserPermission manageAgents = new UserPermission();
		manageAgents.setUser(user);
		manageAgents.setGrantedBy(null);
		manageAgents.setPermission(PermissionType.MANAGE_AGENTS);
		userPermissionRepository.save(manageAgents);

		UserPermission validateMerchants = new UserPermission();
		validateMerchants.setUser(user);
		validateMerchants.setGrantedBy(null);
		validateMerchants.setPermission(PermissionType.VALIDATE_MERCHANTS);
		userPermissionRepository.save(validateMerchants);

		logger.info("Association permissions created");
	}
}
