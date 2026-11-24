package com.shoploc.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.shoploc.user.config.SecurityConfig;
import com.shoploc.user.dto.LoginResponse;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.exception.AccountNotActiveException;
import com.shoploc.user.exception.InvalidCredentialsException;
import com.shoploc.user.service.AuthService;

/**
 * Tests the HTTP layer and the security rules, without a database
 * (AuthService is replaced by a fake made with Mockito).
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String BODY = "{\"email\":\"a@b.org\",\"password\":\"secret\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void loginIsPublicAndReturnsTheToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("the-token", "Bearer", 900));

        mockMvc.perform(post("/api/users/auth/login").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("the-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void wrongCredentialsGive401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/users/auth/login").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void refusedMerchantGets403WithTheStatusOfTheRequest() throws Exception {
        when(authService.login(any())).thenThrow(
                new AccountNotActiveException("ACCOUNT_PENDING", MerchantRequestStatus.REFUSED, "Address not found"));

        mockMvc.perform(post("/api/users/auth/login").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_PENDING"))
                .andExpect(jsonPath("$.merchantRequestStatus").value("REFUSED"))
                .andExpect(jsonPath("$.refusalReason").value("Address not found"));
    }

    @Test
    void missingPasswordGives400() throws Exception {
        mockMvc.perform(post("/api/users/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.org\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedRouteWithoutTokenGives401() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRouteWithGarbageTokenGives401() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
