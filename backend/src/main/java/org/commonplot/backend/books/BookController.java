package org.commonplot.backend.books;

import jakarta.validation.Valid;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.commonplot.backend.logging.LoggingService;
import org.commonplot.backend.meetups.MeetupService;
import org.commonplot.backend.meetups.model.Meetup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(originPatterns = "*")
public class BookController {

    private final BookService    bookService;
    private final MeetupService  meetupService;
    private final LoggingService loggingService;

    public BookController(BookService bookService, MeetupService meetupService,
                          LoggingService loggingService) {
        this.bookService    = bookService;
        this.meetupService  = meetupService;
        this.loggingService = loggingService;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAll() {
        return ResponseEntity.ok(bookService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Integer id) {
        return bookService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> create(
            @Valid @RequestBody BookRequest request,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        Book created = bookService.create(request);
        loggingService.log(null, username != null ? username : "unknown",
                role != null ? role : "USER",
                "CREATE_BOOK", "Created book ID " + created.getId() + ": " + created.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> update(
            @PathVariable Integer id,
            @Valid @RequestBody BookRequest request,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        return bookService.update(id, request)
                .map(updated -> {
                    loggingService.log(null, username != null ? username : "unknown",
                            role != null ? role : "USER",
                            "UPDATE_BOOK", "Updated book ID " + id + ": " + updated.getTitle());
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        List<Meetup> meetups = meetupService.getMeetupsByBookId(id);
        for (Meetup m : meetups) meetupService.delete(m.getId());

        if (bookService.delete(id)) {
            loggingService.log(null, username != null ? username : "unknown",
                    role != null ? role : "USER",
                    "DELETE_BOOK", "Deleted book ID " + id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("total", bookService.count()));
    }

    @GetMapping("/{id}/meetups")
    public ResponseEntity<List<Meetup>> getMeetupsByBook(@PathVariable Integer id) {
        if (bookService.getById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(meetupService.getMeetupsByBookId(id));
    }

    @GetMapping("/{id}/meetups/count")
    public ResponseEntity<Map<String, Object>> countMeetupsByBook(@PathVariable Integer id) {
        if (bookService.getById(id).isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("bookId", id,
                "count", meetupService.getMeetupsByBookId(id).size()));
    }

    @GetMapping("/{id}/meetups/stats")
    public ResponseEntity<Map<String, Object>> statsByBook(@PathVariable Integer id) {
        if (bookService.getById(id).isEmpty()) return ResponseEntity.notFound().build();

        List<Meetup> meetups = meetupService.getMeetupsByBookId(id);
        double avgRating = meetups.stream()
                .filter(m -> m.getRating() != null)
                .mapToDouble(Meetup::getRating).average().orElse(0.0);
        double avgDuration = meetups.stream()
                .filter(m -> m.getDuration() != null)
                .mapToDouble(Meetup::getDuration).average().orElse(0.0);

        return ResponseEntity.ok(Map.of(
                "bookId",       id,
                "totalMeetups", meetups.size(),
                "avgRating",    Math.round(avgRating    * 10.0) / 10.0,
                "avgDuration",  Math.round(avgDuration)
        ));
    }
}