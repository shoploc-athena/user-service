package com.shoploc.user.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import com.shoploc.user.config.SecurityConfig;
import com.shoploc.user.config.ShoplocProperties;
import com.shoploc.user.config.ShoplocProperties.JwtProperties;

/** Checks the rules of the decoder: signature, expiration, city, and start-up settings. */
class JwtValidationTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test";

    private final SecurityConfig config = new SecurityConfig();
    private final ShoplocProperties lille = new ShoplocProperties("lille", new JwtProperties(SECRET, 15));
    private final JwtDecoder decoder = config.jwtDecoder(lille);

    private String token(ShoplocProperties props, String secret, Instant expiresAt) {
        JwtEncoder encoder = config.jwtEncoder(new ShoplocProperties(props.city(), new JwtProperties(secret, 15)));
        Instant issuedAt = expiresAt.minus(15, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(UUID.randomUUID().toString())
                .claim("city", props.city())
                .claim("role", "CUSTOMER")
                .claim("permissions", List.of())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @Test
    void validTokenIsAccepted() {
        String token = token(lille, SECRET, Instant.now().plus(10, ChronoUnit.MINUTES));

        assertThat(decoder.decode(token).getClaimAsString("city")).isEqualTo("lille");
    }

    @Test
    void expiredTokenIsRejected() {
        String token = token(lille, SECRET, Instant.now().minus(1, ChronoUnit.HOURS));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = token(lille, "another-secret-another-secret-12345", Instant.now().plus(10, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void modifiedTokenIsRejected() {
        String token = token(lille, SECRET, Instant.now().plus(10, ChronoUnit.MINUTES));
        // We change the first character of the signature part.
        int lastDot = token.lastIndexOf('.');
        char first = token.charAt(lastDot + 1);
        String modified = token.substring(0, lastDot + 1) + (first == 'A' ? 'B' : 'A') + token.substring(lastDot + 2);

        assertThatThrownBy(() -> decoder.decode(modified)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenFromAnotherCityIsRejected() {
        ShoplocProperties paris = new ShoplocProperties("paris", new JwtProperties(SECRET, 15));
        String token = token(paris, SECRET, Instant.now().plus(10, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void serviceRefusesToStartWithAShortSecret() {
        JwtProperties shortSecret = new JwtProperties("too-short", 15);

        assertThatThrownBy(() -> new ShoplocProperties("lille", shortSecret))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void serviceRefusesToStartWithoutCity() {
        JwtProperties jwt = new JwtProperties(SECRET, 15);

        assertThatThrownBy(() -> new ShoplocProperties("", jwt))
                .isInstanceOf(IllegalStateException.class);
    }
}