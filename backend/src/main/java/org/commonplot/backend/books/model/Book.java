package org.commonplot.backend.books.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ============================================================
//  Book.java — entitate JPA
//  Tabel: books
//  Relație: 1 Book → N Meetups
// ============================================================
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String author;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @Size(max = 500)
    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    // ── Constructors ──────────────────────────────────────────
    public Book() {}

    public Book(String title, String author, String description, String coverUrl) {
        this.title       = title;
        this.author      = author;
        this.description = description;
        this.coverUrl    = coverUrl;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Integer getId()                 { return id; }
    public void setId(Integer id)          { this.id = id; }

    public String getTitle()               { return title; }
    public void setTitle(String t)         { this.title = t; }

    public String getAuthor()              { return author; }
    public void setAuthor(String a)        { this.author = a; }

    public String getDescription()         { return description; }
    public void setDescription(String d)   { this.description = d; }

    public String getCoverUrl()            { return coverUrl; }
    public void setCoverUrl(String c)      { this.coverUrl = c; }
}