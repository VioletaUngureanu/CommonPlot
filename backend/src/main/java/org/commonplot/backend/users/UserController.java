package org.commonplot.backend.users;

import org.commonplot.backend.logging.LoggingService;
import org.commonplot.backend.users.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "*")
public class UserController {

    private final UserService    userService;
    private final LoggingService loggingService;

    public UserController(UserService userService, LoggingService loggingService) {
        this.userService    = userService;
        this.loggingService = loggingService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required."));

        var result = userService.login(username, password);

        if (result.isEmpty()) {
            loggingService.log(null, username, "UNKNOWN",
                    "LOGIN_FAILED", "Failed login attempt for: " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }

        User user = result.get();
        loggingService.log(user.getId(), user.getUsername(),
                user.getRole().getName(), "LOGIN_SUCCESS", "User logged in");

        return ResponseEntity.ok(Map.of(
                "id",          user.getId(),
                "username",    user.getUsername(),
                "email",       user.getEmail(),
                "fullName",    user.getFullName() != null ? user.getFullName() : "",
                "role",        user.getRole().getName(),
                "permissions", user.getRole().getPermissions()
                        .stream().map(p -> p.getName()).toList()
        ));
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(
                    body.get("username"), body.get("password"),
                    body.get("email"),    body.get("fullName")
            );
            loggingService.log(user.getId(), user.getUsername(),
                    "USER", "REGISTER", "New user registered");
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id",       user.getId(),
                    "username", user.getUsername(),
                    "role",     user.getRole().getName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/api/users/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return userService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}