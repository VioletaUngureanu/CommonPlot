package org.commonplot.backend.books.model;

import jakarta.validation.constraints.*;


public class BookRequest {

    @NotBlank(message = "Book title is required.")
    @Size(min=2, max=150, message = "Title must be between 2 and 150 characters")
    private String bookTitle;

    @NotBlank(message = "Book author is required.")
    @Size(min=3, max=250, message = "Title must be between 2 and 150 characters")
    private String bookAuthor;

    @NotBlank(message = "Book description is required.")
    @Size(min=3, max=500, message = "Title must be between 2 and 150 characters")
    private String bookDescription;


    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public void setBookDescription(String bookDescription) {
        this.bookDescription = bookDescription;
    }
}
