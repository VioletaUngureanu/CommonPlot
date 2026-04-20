package org.commonplot.backend.meetups.model;

import jakarta.validation.constraints.*;

public class MeetupRequest {

    @NotBlank(message = "Event title is required.")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters.")
    private String titleEvent;

    @NotBlank(message = "Location is required.")
    @Size(min = 3, max = 100, message = "Location must be between 3 and 100 characters.")
    private String location;

    @NotBlank(message = "Date is required.")
    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$",
            message = "Date must be in ISO 8601 format: yyyy-MM-ddTHH:mm"
    )
    private String date;

    @NotNull(message = "Book ID is required.")
    @Positive(message = "Book ID must be a positive integer.")
    private Integer bookId;

    private String bookTitle;
    private String bookAuthor;

    @NotNull(message = "Owner ID is required.")
    @Positive(message = "Owner ID must be a positive integer.")
    private Integer ownerID;

    @NotBlank(message = "Owner username is required.")
    private String ownerUsername;

    @NotNull(message = "Duration is required.")
    @Min(value = 15,  message = "Duration must be at least 15 minutes.")
    @Max(value = 480, message = "Duration cannot exceed 480 minutes.")
    private Integer duration;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.")
    private Double rating;

    @Size(max = 500, message = "Description cannot exceed 500 characters.")
    private String description;

    // ── Getters & Setters ─────────────────────────────────────
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