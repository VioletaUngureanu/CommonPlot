package org.commonplot.backend.users.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

// ============================================================
//  Role.java — entitate JPA
//  Tabel: roles
//  Relație: 1 Role → N Users, N Roles ↔ N Permissions
// ============================================================
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns        = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role() {}

    public Role(String name) { this.name = name; }

    public Integer getId()                         { return id; }
    public void    setId(Integer id)               { this.id = id; }

    public String  getName()                       { return name; }
    public void    setName(String name)            { this.name = name; }

    public Set<Permission> getPermissions()        { return permissions; }
    public void setPermissions(Set<Permission> p)  { this.permissions = p; }
}