package org.commonplot.backend.books;

import org.commonplot.backend.books.model.BookRequest;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.meetups.MeetupService;
import org.commonplot.backend.meetups.model.Meetup;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

// ============================================================
//  BookGraphQLController.java — Gold: GraphQL pentru Books
//  Același BookService + MeetupService, interfață GraphQL
// ============================================================
@Controller
public class BookGraphQLController {

    private final BookService   bookService;
    private final MeetupService meetupService;

    public BookGraphQLController(BookService bookService, MeetupService meetupService) {
        this.bookService   = bookService;
        this.meetupService = meetupService;
    }

    // ── Queries ───────────────────────────────────────────────

    @QueryMapping
    public List<Book> books() {
        return bookService.getAll();
    }

    @QueryMapping
    public Book book(@Argument Integer id) {
        return bookService.getById(id).orElse(null);
    }

    @QueryMapping
    public List<Meetup> meetupsByBook(@Argument Integer bookId) {
        return meetupService.getMeetupsByBookId(bookId);
    }

    @QueryMapping
    public Map<String, Object> bookStats(@Argument Integer bookId) {
        List<Meetup> meetups = meetupService.getMeetupsByBookId(bookId);

        double avgRating = meetups.stream()
                .filter(m -> m.getRating() != null)
                .mapToDouble(Meetup::getRating)
                .average().orElse(0.0);

        double avgDuration = meetups.stream()
                .filter(m -> m.getDuration() != null)
                .mapToDouble(Meetup::getDuration)
                .average().orElse(0.0);

        return Map.of(
                "bookId",       bookId,
                "totalMeetups", meetups.size(),
                "avgRating",    Math.round(avgRating    * 10.0) / 10.0,
                "avgDuration",  Math.round(avgDuration)
        );
    }

    // ── Mutations ─────────────────────────────────────────────

    @MutationMapping
    public Book createBook(@Argument Map<String, Object> input) {
        return bookService.create(mapToRequest(input));
    }

    @MutationMapping
    public Book updateBook(@Argument Integer id, @Argument Map<String, Object> input) {
        return bookService.update(id, mapToRequest(input)).orElse(null);
    }

    @MutationMapping
    public boolean deleteBook(@Argument Integer id) {
        return bookService.delete(id);
    }

    // ── Mapper ────────────────────────────────────────────────
    private BookRequest mapToRequest(Map<String, Object> input) {
        BookRequest req = new BookRequest();
        req.setTitle((String) input.get("title"));
        req.setAuthor((String) input.get("author"));
        req.setDescription((String) input.get("description"));
        req.setCoverUrl((String) input.get("coverUrl"));
        return req;
    }
}