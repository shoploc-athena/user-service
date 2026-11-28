package com.shoploc.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import com.shoploc.user.dto.LoginRequest;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.UserRole;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.exception.AccountNotActiveException;
import com.shoploc.user.exception.MerchantRequestAlreadyDecidedException;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.MerchantRequestRepository;
import com.shoploc.user.service.AuthService;
import com.shoploc.user.service.MerchantDecisionService;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

/**
 * Integration test: real database (Testcontainers), real security rules, real services.
 * The test is NOT @Transactional on purpose: each decision must really be committed.
 */
@SpringBootTest
@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection", "SqlSourceToSinkFlow"})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MerchantDecisionIntegrationTest {

    private static final String PASSWORD = "correct-password-123";
    private static final String BASE_URL = "/api/users/merchant-requests/";
    private static final String VALIDATE_MERCHANTS = "PERM_VALIDATE_MERCHANTS";
    private static final String CODE = "$.code";
    private static final String ALREADY_DECIDED = "MERCHANT_REQUEST_ALREADY_DECIDED";

    private static final String CREATE_FAILING_FUNCTION = """
            CREATE OR REPLACE FUNCTION fail_user_update() RETURNS trigger AS $$
            BEGIN
                RAISE EXCEPTION 'simulated failure';
            END;
            $$ LANGUAGE plpgsql
            """;
    private static final String CREATE_FAILING_TRIGGER =
            "CREATE TRIGGER fail_user_update BEFORE UPDATE ON app_user "
                    + "FOR EACH ROW EXECUTE FUNCTION fail_user_update()";
    private static final String DROP_FAILING_TRIGGER = "DROP TRIGGER IF EXISTS fail_user_update ON app_user";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private MerchantRequestRepository merchantRequestRepository;
    @Autowired
    private MerchantDecisionService decisionService;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AppUser merchant;
    private AppUser reviewer;
    private MerchantRequest request;

    @BeforeEach
    void setUp() {
        merchant = saveUser(UserRole.MERCHANT, UserStatus.PENDING);
        reviewer = saveUser(UserRole.ASSOCIATION, UserStatus.ACTIVE);
        request = merchantRequestRepository.save(new MerchantRequest(merchant, "Bakery", "1 rue de Lille"));
    }

    @AfterEach
    void cleanUp() {
        merchantRequestRepository.deleteById(request.getId());
        appUserRepository.deleteById(merchant.getId());
        appUserRepository.deleteById(reviewer.getId());
    }

    // ---------- rights ----------

    @Test
    void withoutTokenGives401() throws Exception {
        mockMvc.perform(post(url("accept"))).andExpect(status().isUnauthorized());

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
    }

    @Test
    void withoutTheRightGives403() throws Exception {
        RequestPostProcessor customer = jwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

        mockMvc.perform(post(url("accept")).with(customer)).andExpect(status().isForbidden());

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
        assertThat(userStatus()).isEqualTo(UserStatus.PENDING);
    }

    // ---------- accept ----------

    @Test
    void acceptingActivatesTheAccountAndTheMerchantCanLogIn() throws Exception {
        accept().andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.reviewedBy").value(reviewer.getId().toString()));

        assertThat(reloadedRequest().getReviewedAt()).isNotNull();
        assertThat(userStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(authService.login(new LoginRequest(merchant.getEmail(), PASSWORD)).accessToken()).isNotBlank();
    }

    @Test
    void unknownRequestGives404() throws Exception {
        mockMvc.perform(post(BASE_URL + UUID.randomUUID() + "/accept").with(reviewerToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(CODE).value("MERCHANT_REQUEST_NOT_FOUND"));
    }

    // ---------- reject ----------

    @Test
    void rejectingKeepsTheAccountPendingAndTheMerchantSeesTheReason() throws Exception {
        reject(reason("Address not found")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUSED"))
                .andExpect(jsonPath("$.refusalReason").value("Address not found"));

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.REFUSED);
        assertThat(userStatus()).isEqualTo(UserStatus.PENDING);

        LoginRequest login = new LoginRequest(merchant.getEmail(), PASSWORD);
        assertThatThrownBy(() -> authService.login(login))
                .isInstanceOfSatisfying(AccountNotActiveException.class,
                        e -> assertThat(e.getRefusalReason()).isEqualTo("Address not found"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"refusalReason\":\"\"}", "{\"refusalReason\":\"   \"}"})
    void rejectingWithoutAReasonGives400(String body) throws Exception {
        reject(body).andExpect(status().isBadRequest());

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
    }

    @Test
    void rejectingWithATooLongReasonGives400() throws Exception {
        reject(reason("a".repeat(256))).andExpect(status().isBadRequest());

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
    }

    // ---------- decision made twice ----------

    @Test
    void aSecondDecisionGives409AndDoesNotChangeTheAccount() throws Exception {
        accept().andExpect(status().isOk());

        accept().andExpect(status().isConflict()).andExpect(jsonPath(CODE).value(ALREADY_DECIDED));
        reject(reason("Too late")).andExpect(status().isConflict()).andExpect(jsonPath(CODE).value(ALREADY_DECIDED));

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.ACCEPTED);
        assertThat(userStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    // ---------- two decisions at the same time ----------

    @Test
    void onlyOneOfTwoSimultaneousDecisionsSucceeds() throws Exception {
        UUID requestId = request.getId();
        UUID reviewerId = reviewer.getId();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> accepted = pool.submit(
                    () -> decide(start, () -> decisionService.accept(requestId, reviewerId)));
            Future<Boolean> rejected = pool.submit(
                    () -> decide(start, () -> decisionService.reject(requestId, reviewerId, "No")));
            start.countDown();

            List<Boolean> outcomes = List.of(accepted.get(10, TimeUnit.SECONDS), rejected.get(10, TimeUnit.SECONDS));

            assertThat(outcomes).containsExactlyInAnyOrder(true, false);
        } finally {
            pool.shutdownNow();
        }
    }

    // ---------- transaction ----------

    @Test
    void requestStaysPendingWhenTheAccountUpdateFails() {
        UUID requestId = request.getId();
        UUID reviewerId = reviewer.getId();
        // A trigger in the database makes every update of app_user fail.
        jdbcTemplate.execute(CREATE_FAILING_FUNCTION);
        jdbcTemplate.execute(CREATE_FAILING_TRIGGER);
        try {
            assertThatThrownBy(() -> decisionService.accept(requestId, reviewerId))
                    .isInstanceOf(RuntimeException.class);
        } finally {
            jdbcTemplate.execute(DROP_FAILING_TRIGGER);
        }

        assertThat(reloadedRequest().getStatus()).isEqualTo(MerchantRequestStatus.PENDING);
        assertThat(userStatus()).isEqualTo(UserStatus.PENDING);
    }

    // ---------- helpers ----------

    /** Waits for the signal, then tries the decision. Returns false if it was refused with a conflict. */
    private boolean decide(CountDownLatch start, Runnable decision) throws InterruptedException {
        start.await();
        try {
            decision.run();
            return true;
        } catch (MerchantRequestAlreadyDecidedException | OptimisticLockingFailureException e) {
            return false;
        }
    }

    private AppUser saveUser(UserRole role, UserStatus userStatus) {
        AppUser user = new AppUser();
        user.setEmail(role.name().toLowerCase() + "-" + UUID.randomUUID() + "@example.org");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        user.setStatus(userStatus);
        return appUserRepository.save(user);
    }

    private RequestPostProcessor reviewerToken() {
        return jwt().jwt(token -> token.subject(reviewer.getId().toString()))
                .authorities(new SimpleGrantedAuthority(VALIDATE_MERCHANTS));
    }

    private String url(String action) {
        return BASE_URL + request.getId() + "/" + action;
    }

    private static String reason(String text) {
        return "{\"refusalReason\":\"" + text + "\"}";
    }

    private ResultActions accept() throws Exception {
        return mockMvc.perform(post(url("accept")).with(reviewerToken()));
    }

    private ResultActions reject(String body) throws Exception {
        return mockMvc.perform(post(url("reject")).with(reviewerToken())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private MerchantRequest reloadedRequest() {
        return merchantRequestRepository.findById(request.getId()).orElseThrow();
    }

    private UserStatus userStatus() {
        return appUserRepository.findById(merchant.getId()).orElseThrow().getStatus();
    }
}
