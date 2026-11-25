package com.shoploc.user.controller;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shoploc.user.dto.MerchantRequestResponse;
import com.shoploc.user.dto.RejectRequest;
import com.shoploc.user.service.MerchantDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/** Decisions of the association on merchant requests (right VALIDATE_MERCHANTS, see SecurityConfig). */
@RestController
@RequestMapping("/api/users/merchant-requests")
@SecurityRequirement(name = "bearerAuth")
public class MerchantDecisionController {

    private final MerchantDecisionService decisionService;

    public MerchantDecisionController(MerchantDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Operation(summary = "Accept a merchant request: 200, 401, 403 (right missing), 404, 409 (already decided)")
    @PostMapping("/{id}/accept")
    public MerchantRequestResponse accept(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return decisionService.accept(id, reviewerId(jwt));
    }

    @Operation(summary = "Refuse a merchant request with a reason: 200, 400 (reason missing or too long), 401, 403, 404, 409")
    @PostMapping("/{id}/reject")
    public MerchantRequestResponse reject(@PathVariable UUID id,
                                          @Valid @RequestBody RejectRequest body,
                                          @AuthenticationPrincipal Jwt jwt) {
        return decisionService.reject(id, reviewerId(jwt), body.refusalReason().trim());
    }

    /** The reviewer is the user written in the "sub" claim of the token. */
    private static UUID reviewerId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
