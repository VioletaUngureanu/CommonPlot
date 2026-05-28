package org.commonplot.backend.security;

import jakarta.persistence.*;
import org.commonplot.backend.users.model.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    public PasswordResetToken() {}

    public PasswordResetToken(User user, String token, LocalDateTime expiresAt) {
        this.user      = user;
        this.token     = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.used      = false;
    }

    public Integer       getId()                          { return id; }
    public User          getUser()                        { return user; }
    public void          setUser(User u)                  { this.user = u; }
    public String        getToken()                       { return token; }
    public void          setToken(String t)               { this.token = t; }
    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public LocalDateTime getExpiresAt()                   { return expiresAt; }
    public Boolean       getUsed()                        { return used; }
    public void          setUsed(Boolean u)               { this.used = u; }
}