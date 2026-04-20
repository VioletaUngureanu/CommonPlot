package org.commonplot.backend.meetups.model;

// ============================================================
//  Meetup.java — entitatea principală, stocată în RAM
//  Nu există persistență de niciun fel (fără DB, fără fișiere)
// ============================================================
public class Meetup {

    private Long id;
    private String titleEvent;
    private String location;
    private String date;          // ISO 8601: "2026-05-12T10:30:00"
    private Integer bookId;
    private String bookTitle;
    private String bookAuthor;
    private Integer ownerID;
    private String ownerUsername;
    private Integer duration;     // minute
    private Double rating;        // 0.0 – 5.0
    private String description;

    // ── Constructors ──────────────────────────────────────────
    public Meetup() {}

    public Meetup(Long id, String titleEvent, String location, String date,
                  Integer bookId, String bookTitle, String bookAuthor,
                  Integer ownerID, String ownerUsername,
                  Integer duration, Double rating, String description) {
        this.id            = id;
        this.titleEvent    = titleEvent;
        this.location      = location;
        this.date          = date;
        this.bookId        = bookId;
        this.bookTitle     = bookTitle;
        this.bookAuthor    = bookAuthor;
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

    public Integer getBookId()                { return bookId; }
    public void setBookId(Integer b)          { this.bookId = b; }

    public String getBookTitle()              { return bookTitle; }
    public void setBookTitle(String t)        { this.bookTitle = t; }

    public String getBookAuthor()             { return bookAuthor; }
    public void setBookAuthor(String a)       { this.bookAuthor = a; }

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