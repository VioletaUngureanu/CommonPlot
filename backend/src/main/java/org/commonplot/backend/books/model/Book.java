package org.commonplot.backend.books.model;

// ============================================================
//  Book.java — entitatea de carte, stocată în RAM
//  Relație: 1 Book → N Meetups (prin bookID în Meetup)
// ============================================================
public class Book {

    private Integer id;
    private String title;
    private String author;
    private String description;
    private String coverUrl;

    // ── Constructors ──────────────────────────────────────────
    public Book() {}

    public Book(Integer id, String title, String author,
                String description, String coverUrl) {
        this.id          = id;
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