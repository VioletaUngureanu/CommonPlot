package org.commonplot.backend.books.model;

public class Book {

    private Long id;
    private String titleBook;
    private String bookAuthor;
    private String bookDescription;

    public Book(){}

    public Book(Long id, String titleBook, String bookAuthor, String bookDescription) {
        this.id = id;
        this.titleBook = titleBook;
        this.bookAuthor = bookAuthor;
        this.bookDescription = bookDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleBook() {
        return titleBook;
    }

    public void setTitleBook(String titleBook) {
        this.titleBook = titleBook;
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
