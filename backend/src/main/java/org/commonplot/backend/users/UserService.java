package org.commonplot.backend.users;

import org.commonplot.backend.users.model.Role;
import org.commonplot.backend.users.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// ============================================================
//  UserService.java — login simplu (fără JWT, fără tokens)
//  Tema cere doar persistența, nu autentificare completă
// ============================================================
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // ── Login ─────────────────────────────────────────────────
    // Verifică username + password (plain text — tema nu cere encryption)
    public Optional<User> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
    }

    // ── Register ──────────────────────────────────────────────
    public User register(String username, String password,
                         String email, String fullName) {
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username already taken.");
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already in use.");

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found."));

        User user = new User(username, password, email, fullName, userRole);
        return userRepository.save(user);
    }

    // ── CRUD ──────────────────────────────────────────────────
    public List<User> getAll()                    { return userRepository.findAll(); }
    public Optional<User> getById(Integer id)     { return userRepository.findById(id); }
    public Optional<User> getByUsername(String u) { return userRepository.findByUsername(u); }

    public boolean delete(Integer id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }
}