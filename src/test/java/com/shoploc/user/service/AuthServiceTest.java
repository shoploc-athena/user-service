package com.shoploc.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import com.shoploc.user.config.SecurityConfig;
import com.shoploc.user.config.ShoplocProperties;
import com.shoploc.user.dto.LoginRequest;
import com.shoploc.user.dto.LoginResponse;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.PermissionType;
import com.shoploc.user.entity.UserPermission;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.exception.AccountNotActiveException;
import com.shoploc.user.exception.InvalidCredentialsException;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.MerchantRequestRepository;
import com.shoploc.user.repository.UserPermissionRepository;
import com.shoploc.user.security.JwtService;

/**
 * Unit test: no Spring context and no database (the repositories are fakes made with Mockito).
 */
class AuthServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test";
    private static final String PASSWORD = "correct-password";

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final UserPermissionRepository userPermissionRepository = mock(UserPermissionRepository.class);
    private final MerchantRequestRepository merchantRequestRepository = mock(MerchantRequestRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final ShoplocProperties properties =
            new ShoplocProperties("lille", new ShoplocProperties.JwtProperties(SECRET, 15));
    private final SecurityConfig securityConfig = new SecurityConfig();
    private final JwtDecoder jwtDecoder = securityConfig.jwtDecoder(properties);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService(securityConfig.jwtEncoder(properties), properties);
        authService = new AuthService(appUserRepository, userPermissionRepository,
                merchantRequestRepository, passwordEncoder, jwtService);
    }

    private AppUser user(UserRole role, UserStatus status) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.org");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setStatus(status);
        when(appUserRepository.findByEmail("user@example.org")).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void unknownEmailGivesInvalidCredentials() {
        when(appUserRepository.findByEmail("nobody@example.org")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.org", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void wrongPasswordGivesInvalidCredentials() {
        user(UserRole.CUSTOMER, UserStatus.ACTIVE);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.org", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void emailIsTrimmedAndLowercased() {
        user(UserRole.CUSTOMER, UserStatus.ACTIVE);

        LoginResponse response = authService.login(new LoginRequest("  User@Example.ORG ", PASSWORD));

        assertThat(response.accessToken()).isNotBlank();
    }

    @Test
    void activeAccountGetsATokenWithExpectedClaims() {
        AppUser user = user(UserRole.ASSOCIATION, UserStatus.ACTIVE);
        UserPermission permission = new UserPermission();
        permission.setPermission(PermissionType.VALIDATE_MERCHANTS);
        when(userPermissionRepository.findByUser_Id(user.getId())).thenReturn(List.of(permission));

        LoginResponse response = authService.login(new LoginRequest("user@example.org", PASSWORD));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(15 * 60);

        // We read the token back with the same decoder that the services use.
        Jwt jwt = jwtDecoder.decode(response.accessToken());
        assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
        assertThat(jwt.getClaimAsString("city")).isEqualTo("lille");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ASSOCIATION");
        assertThat(jwt.getClaimAsStringList("permissions")).containsExactly("VALIDATE_MERCHANTS");
    }

    @Test
    void refusedMerchantGetsPendingErrorWithReason() {
        AppUser user = user(UserRole.MERCHANT, UserStatus.PENDING);
        MerchantRequest request = new MerchantRequest();
        request.setStatus(MerchantRequestStatus.REFUSED);
        request.setRefusalReason("Store address not found");
        when(merchantRequestRepository.findFirstByUser_IdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.of(request));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.org", PASSWORD)))
                .isInstanceOfSatisfying(AccountNotActiveException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("ACCOUNT_PENDING");
                    assertThat(e.getMerchantRequestStatus()).isEqualTo(MerchantRequestStatus.REFUSED);
                    assertThat(e.getRefusalReason()).isEqualTo("Store address not found");
                });
    }

    @Test
    void disabledAccountIsRefused() {
        user(UserRole.CUSTOMER, UserStatus.DISABLED);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.org", PASSWORD)))
                .isInstanceOfSatisfying(AccountNotActiveException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ACCOUNT_DISABLED"));
    }
}
