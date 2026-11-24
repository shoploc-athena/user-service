package com.shoploc.user.service;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.dto.LoginRequest;
import com.shoploc.user.dto.LoginResponse;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.exception.AccountNotActiveException;
import com.shoploc.user.exception.InvalidCredentialsException;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.MerchantRequestRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.security.JwtService;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final MerchantRequestRepository merchantRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    // Fake hash, compared when the email does not exist, so the response time stays similar.
    private final String dummyHash;

    public AuthService(AppUserRepository appUserRepository,
                       UserPermissionRepository userPermissionRepository,
                       MerchantRequestRepository merchantRequestRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.merchantRequestRepository = merchantRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Optional<AppUser> found = appUserRepository.findByEmail(email);

        // We always compare with a hash (the real one, or a fake one).
        String hash = found.map(AppUser::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);

        if (found.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        AppUser user = found.get();
        checkAccountCanLogin(user);

        List<String> permissions = userPermissionRepository.findByUser_Id(user.getId()).stream()
                .map(permission -> permission.getPermission().name())
                .toList();

        return jwtService.createToken(user, permissions);
    }

    private void checkAccountCanLogin(AppUser user) {
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new AccountNotActiveException("ACCOUNT_DISABLED", null, null);
        }
        if (user.getStatus() == UserStatus.PENDING) {
            if (user.getRole() == UserRole.MERCHANT) {
                Optional<MerchantRequest> lastRequest =
                        merchantRequestRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId());
                if (lastRequest.isPresent()) {
                    throw new AccountNotActiveException("ACCOUNT_PENDING",
                            lastRequest.get().getStatus(), lastRequest.get().getRefusalReason());
                }
            }
            throw new AccountNotActiveException("ACCOUNT_PENDING", null, null);
        }
    }
}
