package com.shoploc.user.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import com.shoploc.user.config.ShoplocProperties;
import com.shoploc.user.dto.LoginResponse;
import com.shoploc.user.entity.AppUser;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final ShoplocProperties properties;

    public JwtService(JwtEncoder jwtEncoder, ShoplocProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public LoginResponse createToken(AppUser user, List<String> permissions) {
        Instant now = Instant.now();
        long minutes = properties.jwt().expirationMinutes();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .claim("city", properties.city())
                .claim("role", user.getRole().name())
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiresAt(now.plus(minutes, ChronoUnit.MINUTES))
                .build();

        // The header must say HS256, otherwise the encoder does not know how to sign.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(token, "Bearer", minutes * 60);
    }
}
