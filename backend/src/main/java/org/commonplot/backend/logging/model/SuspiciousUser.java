package org.commonplot.backend.logging.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
//  SuspiciousUser.java — entitate JPA
//  Tabel: suspicious_users
//  Useri marcați automat de mecanismul de detecție
// ============================================================
@Entity
@Table(name = "suspicious_users")
public class SuspiciousUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean resolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    public SuspiciousUser() {}

    public SuspiciousUser(Integer userId, String username, String reason) {
        this.userId     = userId;
        this.username   = username;
        this.reason     = reason;
        this.detectedAt = LocalDateTime.now();
        this.resolved   = false;
    }

    public Integer       getId()                           { return id; }
    public void          setId(Integer id)                 { this.id = id; }
    public Integer       getUserId()                       { return userId; }
    public void          setUserId(Integer u)              { this.userId = u; }
    public String        getUsername()                     { return username; }
    public void          setUsername(String u)             { this.username = u; }
    public String        getReason()                       { return reason; }
    public void          setReason(String r)               { this.reason = r; }
    public LocalDateTime getDetectedAt()                   { return detectedAt; }
    public void          setDetectedAt(LocalDateTime t)    { this.detectedAt = t; }
    public Boolean       getResolved()                     { return resolved; }
    public void          setResolved(Boolean r)            { this.resolved = r; }
    public LocalDateTime getResolvedAt()                   { return resolvedAt; }
    public void          setResolvedAt(LocalDateTime t)    { this.resolvedAt = t; }
    public String        getResolvedBy()                   { return resolvedBy; }
    public void          setResolvedBy(String r)           { this.resolvedBy = r; }
}