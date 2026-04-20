package org.commonplot.backend.books;

import org.commonplot.backend.PagedResponse;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.meetups.model.MeetupRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class BooksGraphQLController {
    private final BookService bookService;

    public BooksGraphQLController(BookService bookService) {
        this.bookService = bookService;
    }

    @QueryMapping
    public PagedResponse<Book> books(
            @Argument Integer page,
            @Argument Integer size
    ) {
        return bookService.getAll(
                page != null ? page : 0,
                size != null ? size : 10
        );
    }
    @QueryMapping
    public Book book(@Argument Long id) {
        return bookService.getById(id).orElse(null);
    }


    // ── Mutations ─────────────────────────────────────────────

    @MutationMapping
    public Book createBook(@Argument Map<String, Object> input) {
        return bookService.create(mapToRequest(input));
    }

    @MutationMapping
    public Book updateBook(@Argument Long id, @Argument Map<String, Object> input) {
        return bookService.update(id, mapToRequest(input)).orElse(null);
    }

    @MutationMapping
    public boolean deleteBook(@Argument Long id) {
        return bookService.delete(id);
    }

    @MutationMapping
    public boolean startGenerator(@Argument Integer interval) {
        bookService.startGenerator(interval != null ? interval : 2);
        return true;
    }

    @MutationMapping
    public boolean stopGenerator() {
        bookService.stopGenerator();
        return true;
    }

    // ── Mapper ────────────────────────────────────────────────
    private BookRequest mapToRequest(Map<String, Object> input) {
        BookRequest req = new BookRequest();
        req.setBookTitle((String) input.get("titleBook"));
        req.setBookAuthor((String) input.get("bookAuthor"));
        req.setBookDescription((String) input.get("bookDescription"));
        return req;
    }
}
