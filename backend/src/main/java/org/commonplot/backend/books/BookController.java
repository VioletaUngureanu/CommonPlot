package org.commonplot.backend.books;

import jakarta.validation.Valid;
import org.commonplot.backend.PagedResponse;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
    // ══════════════════════════════════════════════════════════
    //  CRUD Endpoints
    // ══════════════════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<PagedResponse<Book>> getAll(@RequestParam(defaultValue = "0")  int page,
                                                      @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0)  return ResponseEntity.badRequest().build();
        if (size < 1 || size > 100) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(bookService.getAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        return bookService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        Book created = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Book> update(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request
    ) {
        return bookService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (bookService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/generator/start")
    public ResponseEntity<Map<String, String>> startGenerator(
            @RequestParam(defaultValue = "2") int interval
    ) {
        if (interval < 1 || interval > 60) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Interval must be between 1 and 60 seconds."));
        }
        bookService.startGenerator(interval);
        return ResponseEntity.ok(Map.of(
                "status", "started",
                "interval", interval + "s"
        ));
    }

    @PostMapping("/generator/stop")
    public ResponseEntity<Map<String, String>> stopGenerator() {
        bookService.stopGenerator();
        return ResponseEntity.ok(Map.of("status", "stopped"));
    }
    @GetMapping("/generator/status")
    public ResponseEntity<Map<String, Boolean>> generatorStatus() {
        return ResponseEntity.ok(Map.of("running", bookService.isGeneratorRunning()));
    }

}
