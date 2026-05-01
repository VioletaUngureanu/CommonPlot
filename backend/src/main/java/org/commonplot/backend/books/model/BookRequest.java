package org.commonplot.backend.books.model;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

// ============================================================
//  BookRequest.java — DTO pentru creare și actualizare Book
//  Validare declarativă cu Jakarta Bean Validation
// ============================================================
public class BookRequest {

    @NotBlank(message = "Title is required.")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters.")
    private String title;

    @NotBlank(message = "Author is required.")
    @Size(min = 2, max = 100, message = "Author must be between 2 and 100 characters.")
    private String author;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
    private String description;

    @URL(message = "Cover URL must be a valid URL (starting with http:// or https://).")
    private String coverUrl;

    // ── Getters & Setters ─────────────────────────────────────
    public String getTitle()               { return title; }
    public void setTitle(String t)         { this.title = t; }

    public String getAuthor()              { return author; }
    public void setAuthor(String a)        { this.author = a; }

    public String getDescription()         { return description; }
    public void setDescription(String d)   { this.description = d; }

    public String getCoverUrl()            { return coverUrl; }
    public void setCoverUrl(String c)      { this.coverUrl = c; }
}