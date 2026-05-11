package org.commonplot.backend.users.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
//  User.java — entitate JPA
//  Tabel: users
//  Relație: N Users → 1 Role (@ManyToOne)
// ============================================================
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String password, String email,
                String fullName, Role role) {
        this.username  = username;
        this.password  = password;
        this.email     = email;
        this.fullName  = fullName;
        this.role      = role;
        this.createdAt = LocalDateTime.now();
    }

    // Helper — verifică dacă userul are o permisiune
    public boolean hasPermission(String permissionName) {
        if (role == null) return false;
        return role.getPermissions().stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    // Helper — verifică dacă e admin
    public boolean isAdmin() {
        return role != null && "ADMIN".equals(role.getName());
    }

    public Integer       getId()                        { return id; }
    public void          setId(Integer id)              { this.id = id; }

    public String        getUsername()                  { return username; }
    public void          setUsername(String u)          { this.username = u; }

    public String        getPassword()                  { return password; }
    public void          setPassword(String p)          { this.password = p; }

    public String        getEmail()                     { return email; }
    public void          setEmail(String e)             { this.email = e; }

    public String        getFullName()                  { return fullName; }
    public void          setFullName(String f)          { this.fullName = f; }

    public Role          getRole()                      { return role; }
    public void          setRole(Role r)                { this.role = r; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }
}