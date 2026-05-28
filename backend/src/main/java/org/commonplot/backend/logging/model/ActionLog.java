package org.commonplot.backend.logging.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
//  ActionLog.java — entitate JPA
//  Tabel: action_logs
//  Fiecare acțiune a unui user e persistată aici
// ============================================================
@Entity
@Table(name = "action_logs")
public class ActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "group_id", nullable = false, length = 20)
    private String groupId;       // 'ADMIN' | 'USER'

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;    // 'LOGIN_FAILED', 'CREATE_MEETUP', etc.

    @Column(name = "action_info", columnDefinition = "TEXT")
    private String actionInfo;    // detalii extra

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public ActionLog() {}

    public ActionLog(Integer userId, String username, String groupId,
                     String actionType, String actionInfo) {
        this.userId     = userId;
        this.username   = username;
        this.groupId    = groupId;
        this.actionType = actionType;
        this.actionInfo = actionInfo;
        this.timestamp  = LocalDateTime.now();
    }

    public Long          getId()                        { return id; }
    public void          setId(Long id)                 { this.id = id; }
    public Integer       getUserId()                    { return userId; }
    public void          setUserId(Integer u)           { this.userId = u; }
    public String        getUsername()                  { return username; }
    public void          setUsername(String u)          { this.username = u; }
    public String        getGroupId()                   { return groupId; }
    public void          setGroupId(String g)           { this.groupId = g; }
    public String        getActionType()                { return actionType; }
    public void          setActionType(String a)        { this.actionType = a; }
    public String        getActionInfo()                { return actionInfo; }
    public void          setActionInfo(String a)        { this.actionInfo = a; }
    public String        getIpAddress()                 { return ipAddress; }
    public void          setIpAddress(String ip)        { this.ipAddress = ip; }
    public LocalDateTime getTimestamp()                 { return timestamp; }
    public void          setTimestamp(LocalDateTime t)  { this.timestamp = t; }
}
