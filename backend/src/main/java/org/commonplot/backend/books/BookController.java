package org.commonplot.backend.books;

import jakarta.validation.Valid;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.commonplot.backend.meetups.MeetupService;
import org.commonplot.backend.meetups.model.Meetup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ============================================================
//  BookController.java — REST endpoints pentru Books
//
//  Injectează AMBELE servicii:
//  - BookService  → CRUD cărți
//  - MeetupService → relația 1-to-many (Book → Meetups)
//
//  Astfel BookService nu știe de MeetupService și
//  MeetupService nu știe de BookService → zero circular dep.
// ============================================================
@RestController
@RequestMapping("/api/books")
@CrossOrigin(originPatterns = "*")
public class BookController {

    private final BookService   bookService;
    private final MeetupService meetupService;

    public BookController(BookService bookService, MeetupService meetupService) {
        this.bookService   = bookService;
        this.meetupService = meetupService;
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD Books
    // ══════════════════════════════════════════════════════════

    /** GET /api/books */
    @GetMapping
    public ResponseEntity<List<Book>> getAll() {
        return ResponseEntity.ok(bookService.getAll());
    }

    /** GET /api/books/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Integer id) {
        return bookService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /api/books */
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.create(request));
    }

    /** PUT /api/books/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(
            @PathVariable Integer id,
            @Valid @RequestBody BookRequest request
    ) {
        return bookService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/books/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        List<Meetup> meetups = meetupService.getMeetupsByBookId(id);
        for (Meetup m : meetups)
            meetupService.delete(m.getId());
        return bookService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** GET /api/books/stats/count */
    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("total", bookService.count()));
    }

    // ══════════════════════════════════════════════════════════
    //  Relație 1-to-many: Book → Meetups
    //  MeetupService e injectat direct în controller —
    //  BookService nu știe nimic de meetup-uri
    // ══════════════════════════════════════════════════════════

    /** GET /api/books/{id}/meetups */
    @GetMapping("/{id}/meetups")
    public ResponseEntity<List<Meetup>> getMeetupsByBook(@PathVariable Integer id) {
        // Verifică că cartea există
        if (bookService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(meetupService.getMeetupsByBookId(id));
    }

    /** GET /api/books/{id}/meetups/count */
    @GetMapping("/{id}/meetups/count")
    public ResponseEntity<Map<String, Object>> countMeetupsByBook(@PathVariable Integer id) {
        if (bookService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        long count = meetupService.getMeetupsByBookId(id).size();
        return ResponseEntity.ok(Map.of("bookId", id, "count", count));
    }

    /** GET /api/books/{id}/meetups/stats */
    @GetMapping("/{id}/meetups/stats")
    public ResponseEntity<Map<String, Object>> statsByBook(@PathVariable Integer id) {
        if (bookService.getById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Meetup> meetups = meetupService.getMeetupsByBookId(id);

        double avgRating = meetups.stream()
                .filter(m -> m.getRating() != null)
                .mapToDouble(Meetup::getRating)
                .average().orElse(0.0);

        double avgDuration = meetups.stream()
                .filter(m -> m.getDuration() != null)
                .mapToDouble(Meetup::getDuration)
                .average().orElse(0.0);

        return ResponseEntity.ok(Map.of(
                "bookId",       id,
                "totalMeetups", meetups.size(),
                "avgRating",    Math.round(avgRating    * 10.0) / 10.0,
                "avgDuration",  Math.round(avgDuration)
        ));
    }
}