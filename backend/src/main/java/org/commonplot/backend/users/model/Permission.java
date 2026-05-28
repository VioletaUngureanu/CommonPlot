package org.commonplot.backend.users.model;

import jakarta.persistence.*;

// ============================================================
//  Permission.java — entitate JPA
//  Tabel: permissions
// ============================================================
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public Permission() {}

    public Permission(String name) { this.name = name; }

    public Integer getId()              { return id; }
    public void    setId(Integer id)    { this.id = id; }

    public String  getName()            { return name; }
    public void    setName(String name) { this.name = name; }
}