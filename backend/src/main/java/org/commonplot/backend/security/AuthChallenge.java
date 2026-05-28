package org.commonplot.backend.security;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_challenges")
public class AuthChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String nonce;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    public AuthChallenge() {}

    public AuthChallenge(String username, String nonce, LocalDateTime expiresAt) {
        this.username  = username;
        this.nonce     = nonce;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.used      = false;
    }

    public Integer       getId()                          { return id; }
    public String        getUsername()                    { return username; }
    public void          setUsername(String u)            { this.username = u; }
    public String        getNonce()                       { return nonce; }
    public void          setNonce(String n)               { this.nonce = n; }
    public LocalDateTime getCreatedAt()                   { return createdAt; }
    public LocalDateTime getExpiresAt()                   { return expiresAt; }
    public void          setExpiresAt(LocalDateTime t)    { this.expiresAt = t; }
    public Boolean       getUsed()                        { return used; }
    public void          setUsed(Boolean u)               { this.used = u; }
}