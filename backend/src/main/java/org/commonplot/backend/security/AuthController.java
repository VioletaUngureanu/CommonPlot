package org.commonplot.backend.security;

import org.commonplot.backend.logging.LoggingService;
import org.commonplot.backend.users.UserRepository;
import org.commonplot.backend.users.UserService;
import org.commonplot.backend.users.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// ============================================================
//  AuthController.java — Bronze/Silver: JWT + 3-way auth
//
//  3-Way Authentication Flow:
//  Step 1: GET  /api/auth/challenge?username=X
//          → Server generează nonce, îl salvează în DB
//          → Returnează { nonce }
//
//  Step 2: Client calculează: response = SHA256(nonce + SHA256(password))
//          POST /api/auth/login { username, response }
//
//  Step 3: Server recalculează și compară
//          Dacă match → returnează JWT token
//
//  Password Recovery:
//  POST /api/auth/forgot-password { email }
//  POST /api/auth/reset-password  { token, newPassword }
//
//  Register (simplu, fără challenge):
//  POST /api/auth/register { username, password, email, fullName }
// ============================================================
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
public class AuthController {

    private final UserService               userService;
    private final UserRepository            userRepository;
    private final JwtService                jwtService;
    private final AuthChallengeRepository   challengeRepository;
    private final PasswordResetTokenRepository resetRepository;
    private final LoggingService            loggingService;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          JwtService jwtService,
                          AuthChallengeRepository challengeRepository,
                          PasswordResetTokenRepository resetRepository,
                          LoggingService loggingService) {
        this.userService         = userService;
        this.userRepository      = userRepository;
        this.jwtService          = jwtService;
        this.challengeRepository = challengeRepository;
        this.resetRepository     = resetRepository;
        this.loggingService      = loggingService;
    }

    // ── Step 1: Generează challenge (nonce) ───────────────────
    @GetMapping("/challenge")
    public ResponseEntity<?> getChallenge(@RequestParam String username) {
        // Verifică că userul există
        if (userRepository.findByUsername(username).isEmpty()) {
            // Nu dezvăluim dacă userul există sau nu
            String fakeNonce = UUID.randomUUID().toString().replace("-", "");
            return ResponseEntity.ok(Map.of("nonce", fakeNonce));
        }

        // Invalidează challenge-ul vechi dacă există
        challengeRepository.findByUsernameAndUsedFalse(username)
                .ifPresent(c -> { c.setUsed(true); challengeRepository.save(c); });

        String nonce = UUID.randomUUID().toString().replace("-", "");
        AuthChallenge challenge = new AuthChallenge(
                username, nonce,
                LocalDateTime.now().plusMinutes(5)
        );
        challengeRepository.save(challenge);

        return ResponseEntity.ok(Map.of("nonce", nonce));
    }

    // ── Step 2 + 3: Login cu challenge-response ───────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username         = body.get("username");
        String clientResponse   = body.get("response");

        if (username == null || clientResponse == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and response are required."));

        // Găsește challenge-ul activ
        var challengeOpt = challengeRepository.findByUsernameAndUsedFalse(username);
        if (challengeOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No active challenge. Request a new one."));

        AuthChallenge challenge = challengeOpt.get();

        // Verifică expiry
        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challenge.setUsed(true);
            challengeRepository.save(challenge);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Challenge expired. Request a new one."));
        }

        // Găsește userul
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            loggingService.log(null, username, "UNKNOWN", "LOGIN_FAILED",
                    "User not found: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials."));
        }

        User user = userOpt.get();

        // Calculează expected response: SHA256(nonce + SHA256(password))
        String hashedPassword    = sha256(user.getPassword());
        String expectedResponse  = sha256(challenge.getNonce() + hashedPassword);

        if (!expectedResponse.equals(clientResponse)) {
            loggingService.log(user.getId(), username,
                    user.getRole().getName(), "LOGIN_FAILED",
                    "Invalid challenge response");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials."));
        }

        // Marchează challenge-ul ca folosit
        challenge.setUsed(true);
        challengeRepository.save(challenge);

        // Generează JWT
        var permissions = user.getRole().getPermissions()
                .stream().map(p -> p.getName()).toList();
        String token = jwtService.generateToken(
                user.getId(), user.getUsername(),
                user.getRole().getName(), permissions
        );

        loggingService.log(user.getId(), username,
                user.getRole().getName(), "LOGIN_SUCCESS", "JWT issued");

        return ResponseEntity.ok(Map.of(
                "token",       token,
                "id",          user.getId(),
                "username",    user.getUsername(),
                "email",       user.getEmail(),
                "fullName",    user.getFullName() != null ? user.getFullName() : "",
                "role",        user.getRole().getName(),
                "permissions", permissions
        ));
    }

    // ── Register ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(
                    body.get("username"), body.get("password"),
                    body.get("email"),    body.get("fullName")
            );
            loggingService.log(user.getId(), user.getUsername(),
                    "USER", "REGISTER", "New user registered");

            // Auto-login: returnează JWT direct
            var permissions = user.getRole().getPermissions()
                    .stream().map(p -> p.getName()).toList();
            String token = jwtService.generateToken(
                    user.getId(), user.getUsername(),
                    user.getRole().getName(), permissions
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "token",    token,
                    "id",       user.getId(),
                    "username", user.getUsername(),
                    "role",     user.getRole().getName(),
                    "permissions", permissions
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Forgot password ───────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Nu dezvăluim dacă email-ul există
            return ResponseEntity.ok(Map.of("message",
                    "If this email exists, a reset link has been sent."));
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = new PasswordResetToken(
                user, token, LocalDateTime.now().plusHours(1)
        );
        resetRepository.save(resetToken);

        // Simulare email — afișat în consolă pentru demo
        System.out.println("=================================================");
        System.out.println("PASSWORD RESET EMAIL (simulated)");
        System.out.println("To:      " + email);
        System.out.println("Subject: CommonPlot Password Reset");
        System.out.println("Token:   " + token);
        System.out.println("Link:    https://localhost:5173/reset-password?token=" + token);
        System.out.println("Expires: " + resetToken.getExpiresAt());
        System.out.println("=================================================");

        loggingService.log(user.getId(), user.getUsername(),
                user.getRole().getName(), "PASSWORD_RESET_REQUEST",
                "Reset token generated for: " + email);

        return ResponseEntity.ok(Map.of("message",
                "If this email exists, a reset link has been sent."));
    }

    // ── Reset password ────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token       = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token and newPassword are required."));

        if (newPassword.length() < 6)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Password must be at least 6 characters."));

        var resetOpt = resetRepository.findByTokenAndUsedFalse(token);
        if (resetOpt.isEmpty())
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired token."));

        PasswordResetToken resetToken = resetOpt.get();

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetToken.setUsed(true);
            resetRepository.save(resetToken);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token expired."));
        }

        // Actualizează parola
        User user = resetToken.getUser();
        user.setPassword(newPassword);
        userRepository.save(user);

        // Invalidează token-ul
        resetToken.setUsed(true);
        resetRepository.save(resetToken);

        loggingService.log(user.getId(), user.getUsername(),
                user.getRole().getName(), "PASSWORD_RESET_SUCCESS",
                "Password reset successfully");

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }

    // ── SHA256 helper ─────────────────────────────────────────
    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}