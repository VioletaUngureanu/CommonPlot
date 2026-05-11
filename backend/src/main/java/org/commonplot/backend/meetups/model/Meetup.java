package org.commonplot.backend.meetups.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.commonplot.backend.books.model.Book;

// ============================================================
//  Meetup.java — entitate JPA
//  Tabel: meetups
//  Relație: N Meetups → 1 Book (@ManyToOne)
//
//  3NF: bookTitle și bookAuthor NU mai sunt stocate în meetup
//  — se accesează prin relația book.getTitle(), book.getAuthor()
// ============================================================
@Entity
@Table(name = "meetups")
public class Meetup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "title_event", nullable = false, length = 150)
    private String titleEvent;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String location;

    @NotBlank
    @Column(nullable = false)
    private String date;

    // ── Relație @ManyToOne: N meetup-uri → 1 carte ────────────
    // Stochează doar book_id (FK) în tabel — nu duplicăm title/author
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerID;

    @NotBlank
    @Size(max = 100)
    @Column(name = "owner_username", nullable = false, length = 100)
    private String ownerUsername;

    @Min(15) @Max(480)
    @Column(nullable = false)
    private Integer duration;

    @Column(columnDefinition = "DECIMAL(3,1)")
    private Double rating;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    // ── Constructors ──────────────────────────────────────────
    public Meetup() {}

    public Meetup(String titleEvent, String location, String date,
                  Book book, Integer ownerID, String ownerUsername,
                  Integer duration, Double rating, String description) {
        this.titleEvent    = titleEvent;
        this.location      = location;
        this.date          = date;
        this.book          = book;
        this.ownerID       = ownerID;
        this.ownerUsername = ownerUsername;
        this.duration      = duration;
        this.rating        = rating;
        this.description   = description;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId()                       { return id; }
    public void setId(Long id)                { this.id = id; }

    public String getTitleEvent()             { return titleEvent; }
    public void setTitleEvent(String t)       { this.titleEvent = t; }

    public String getLocation()               { return location; }
    public void setLocation(String l)         { this.location = l; }

    public String getDate()                   { return date; }
    public void setDate(String d)             { this.date = d; }

    public Book getBook()                     { return book; }
    public void setBook(Book b)               { this.book = b; }

    // Helper — returnează bookID direct (util pentru compatibilitate frontend)
    public Integer getBookID() {
        return book != null ? book.getId() : null;
    }

    // Helper — returnează bookTitle direct
    public String getBookTitle() {
        return book != null ? book.getTitle() : null;
    }

    // Helper — returnează bookAuthor direct
    public String getBookAuthor() {
        return book != null ? book.getAuthor() : null;
    }

    public Integer getOwnerID()               { return ownerID; }
    public void setOwnerID(Integer o)         { this.ownerID = o; }

    public String getOwnerUsername()          { return ownerUsername; }
    public void setOwnerUsername(String u)    { this.ownerUsername = u; }

    public Integer getDuration()              { return duration; }
    public void setDuration(Integer d)        { this.duration = d; }

    public Double getRating()                 { return rating; }
    public void setRating(Double r)           { this.rating = r; }

    public String getDescription()            { return description; }
    public void setDescription(String d)      { this.description = d; }
}