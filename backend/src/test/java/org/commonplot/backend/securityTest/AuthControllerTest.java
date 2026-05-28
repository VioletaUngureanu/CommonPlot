package org.commonplot.backend.securityTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonplot.backend.logging.LoggingService;
import org.commonplot.backend.security.AuthChallenge;
import org.commonplot.backend.security.AuthChallengeRepository;
import org.commonplot.backend.security.AuthController;
import org.commonplot.backend.security.JwtService;
import org.commonplot.backend.security.PasswordResetToken;
import org.commonplot.backend.security.PasswordResetTokenRepository;
import org.commonplot.backend.users.UserRepository;
import org.commonplot.backend.users.UserService;
import org.commonplot.backend.users.model.Role;
import org.commonplot.backend.users.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ============================================================
//  AuthControllerTest.java — teste pentru:
//  - 3-way authentication (challenge + login)
//  - Register
//  - Forgot/reset password
// ============================================================
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)

class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private UserService userService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private UserRepository userRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private JwtService jwtService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private AuthChallengeRepository challengeRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private PasswordResetTokenRepository resetRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private LoggingService loggingService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helpers ───────────────────────────────────────────────
    private Role sampleRole() {
        Role role = new Role("USER");
        role.setId(2);
        role.setPermissions(Set.of());
        return role;
    }

    private User sampleUser() {
        User user = new User("testuser", "pass123",
                "test@commonplot.com", "Test User", sampleRole());
        user.setId(1);
        return user;
    }

    private AuthChallenge sampleChallenge() {
        return new AuthChallenge("testuser", "abc123nonce",
                LocalDateTime.now().plusMinutes(5));
    }

    // ── GET /api/auth/challenge ───────────────────────────────

    @Test
    void challenge_returns200WithNonceWhenUserExists() throws Exception {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(sampleUser()));
        when(challengeRepository.findByUsernameAndUsedFalse("testuser"))
                .thenReturn(Optional.empty());
        when(challengeRepository.save(any())).thenReturn(sampleChallenge());

        mockMvc.perform(get("/api/auth/challenge?username=testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nonce").exists());
    }

    @Test
    void challenge_returns200WithFakeNonceWhenUserNotExists() throws Exception {
        // Nu dezvăluim dacă userul există sau nu
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/challenge?username=unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nonce").exists());
    }

    @Test
    void challenge_invalidatesOldChallengeWhenExists() throws Exception {
        AuthChallenge oldChallenge = sampleChallenge();
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(sampleUser()));
        when(challengeRepository.findByUsernameAndUsedFalse("testuser"))
                .thenReturn(Optional.of(oldChallenge));
        when(challengeRepository.save(any())).thenReturn(sampleChallenge());

        mockMvc.perform(get("/api/auth/challenge?username=testuser"))
                .andExpect(status().isOk());

        // Verifică că challenge-ul vechi a fost marcat ca folosit
        verify(challengeRepository, atLeastOnce()).save(any());
    }

    // ── POST /api/auth/login ──────────────────────────────────

    @Test
    void login_returns401WhenNoChallengeExists() throws Exception {
        when(challengeRepository.findByUsernameAndUsedFalse("testuser"))
                .thenReturn(Optional.empty());

        Map<String, String> body = Map.of(
                "username", "testuser",
                "response", "someresponse"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void login_returns401WhenChallengeExpired() throws Exception {
        AuthChallenge expired = new AuthChallenge("testuser", "nonce123",
                LocalDateTime.now().minusMinutes(1)); // expirat
        when(challengeRepository.findByUsernameAndUsedFalse("testuser"))
                .thenReturn(Optional.of(expired));

        Map<String, String> body = Map.of(
                "username", "testuser",
                "response", "someresponse"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returns400WhenMissingFields() throws Exception {
        Map<String, String> body = Map.of("username", "testuser");
        // lipsește "response"

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns401WhenWrongResponse() throws Exception {
        AuthChallenge challenge = sampleChallenge();
        when(challengeRepository.findByUsernameAndUsedFalse("testuser"))
                .thenReturn(Optional.of(challenge));
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(sampleUser()));

        Map<String, String> body = Map.of(
                "username", "testuser",
                "response", "wrongresponse"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /api/auth/register ───────────────────────────────

    @Test
    void register_returns201WithValidData() throws Exception {
        User created = sampleUser();
        when(userService.register(any(), any(), any(), any())).thenReturn(created);
        when(jwtService.generateToken(any(), any(), any(), any()))
                .thenReturn("mock.jwt.token");

        Map<String, String> body = new HashMap<>();
        body.put("username", "newuser");
        body.put("password", "pass123");
        body.put("email", "new@commonplot.com");
        body.put("fullName", "New User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void register_returns400WhenUsernameTaken() throws Exception {
        when(userService.register(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Username already taken."));

        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "pass123");
        body.put("email", "admin2@commonplot.com");
        body.put("fullName", "Admin2");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username already taken."));
    }

    @Test
    void register_returns400WhenEmailTaken() throws Exception {
        when(userService.register(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Email already in use."));

        Map<String, String> body = new HashMap<>();
        body.put("username", "newuser2");
        body.put("password", "pass123");
        body.put("email", "admin@commonplot.com");
        body.put("fullName", "New User 2");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already in use."));
    }

    // ── POST /api/auth/forgot-password ────────────────────────

    @Test
    void forgotPassword_returns200WhenEmailExists() throws Exception {
        when(userRepository.findByEmail("test@commonplot.com"))
                .thenReturn(Optional.of(sampleUser()));
        when(resetRepository.save(any())).thenReturn(mock(PasswordResetToken.class));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@commonplot.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void forgotPassword_returns200EvenWhenEmailNotExists() throws Exception {
        // Nu dezvăluim dacă email-ul există
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void forgotPassword_returns400WhenEmailMissing() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/auth/reset-password ─────────────────────────

    @Test
    void resetPassword_returns400WhenTokenInvalid() throws Exception {
        when(resetRepository.findByTokenAndUsedFalse("invalidtoken"))
                .thenReturn(Optional.empty());

        Map<String, String> body = Map.of(
                "token", "invalidtoken",
                "newPassword", "newpass123"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void resetPassword_returns400WhenPasswordTooShort() throws Exception {
        Map<String, String> body = Map.of(
                "token", "validtoken",
                "newPassword", "abc"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_returns400WhenMissingFields() throws Exception {
        Map<String, String> body = Map.of("token", "sometoken");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_returns400WhenTokenExpired() throws Exception {
        User user = sampleUser();
        PasswordResetToken expiredToken = new PasswordResetToken(
                user, "expiredtoken",
                LocalDateTime.now().minusHours(2)  // expirat
        );
        when(resetRepository.findByTokenAndUsedFalse("expiredtoken"))
                .thenReturn(Optional.of(expiredToken));

        Map<String, String> body = Map.of(
                "token", "expiredtoken",
                "newPassword", "newpass123"
        );

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token expired."));
    }
}