package com.shoploc.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private UserPermissionRepository userPermissionRepository;

	@Value("${ASSOCIATION_ADMIN_EMAIL:}")
	private String associationAdminEmail;

	@Value("${ASSOCIATION_ADMIN_PASSWORD:}")
	private String associationAdminPassword;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Transactional
	public void initializeFirstAssociationAccount() {
		if (associationAdminEmail == null || associationAdminEmail.isBlank() ||
			associationAdminPassword == null || associationAdminPassword.isBlank()) {
			logger.warn("Association admin credentials not provided");
			return;
		}

		if (appUserRepository.findByEmail(associationAdminEmail).isPresent()) {
			logger.info("Association account already exists");
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

		AppUser savedUser = appUserRepository.save(associationAdmin);
		logger.info("Association account created: {}", savedUser.getId());

		UserPermission manageAgents = new UserPermission();
		manageAgents.setUser(savedUser);
		manageAgents.setGrantedBy(null);
		manageAgents.setPermission(PermissionType.MANAGE_AGENTS);
		userPermissionRepository.save(manageAgents);

		UserPermission validateMerchants = new UserPermission();
		validateMerchants.setUser(savedUser);
		validateMerchants.setGrantedBy(null);
		validateMerchants.setPermission(PermissionType.VALIDATE_MERCHANTS);
		userPermissionRepository.save(validateMerchants);

		logger.info("Association permissions created");
	}
}
