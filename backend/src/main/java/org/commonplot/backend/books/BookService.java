package org.commonplot.backend.books;

import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// ============================================================
//  BookService.java — RAM storage + CRUD pentru cărți
//
//  IMPORTANT: BookService nu injectează MeetupService.
//  Relația 1-to-many (Book → Meetups) e rezolvată în
//  BookController care injectează ambele servicii direct.
//  Astfel evităm dependențe circulare.
// ============================================================
@Service
public class BookService {

    // ── RAM Storage ───────────────────────────────────────────
    private final ConcurrentHashMap<Integer, Book> store = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    // ── Date mock inițiale ────────────────────────────────────
    public BookService() {
        initMockData();
    }

    private void initMockData() {
        create(new BookRequest() {{
            setTitle("Crime and Punishment");
            setAuthor("Fyodor Dostoevsky");
            setDescription("A psychological novel about guilt and redemption.");
            setCoverUrl("https://covers.openlibrary.org/b/id/14898568-L.jpg");
        }});
        create(new BookRequest() {{
            setTitle("Atomic Habits");
            setAuthor("James Clear");
            setDescription("An easy guide to building good habits and breaking bad ones.");
            setCoverUrl("https://covers.openlibrary.org/b/id/15108516-L.jpg");
        }});
        create(new BookRequest() {{
            setTitle("The Trial");
            setAuthor("Franz Kafka");
            setDescription("A man is prosecuted by an inaccessible authority for an unspecified crime.");
            setCoverUrl("https://covers.openlibrary.org/b/id/15082861-L.jpg");
        }});
        create(new BookRequest() {{
            setTitle("War and Peace");
            setAuthor("Leo Tolstoy");
            setDescription("Epic novel of Russian society during the Napoleonic era.");
            setCoverUrl("https://covers.openlibrary.org/b/id/15111564-L.jpg");
        }});
        create(new BookRequest() {{
            setTitle("The Brothers Karamazov");
            setAuthor("Fyodor Dostoevsky");
            setDescription("A passionate philosophical novel set in 19th-century Russia.");
            setCoverUrl("https://covers.openlibrary.org/b/id/8272329-L.jpg");
        }});
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════════

    public List<Book> getAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Book::getId))
                .collect(Collectors.toList());
    }

    public Optional<Book> getById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    public Book create(BookRequest req) {
        Integer id = idCounter.getAndIncrement();
        Book book  = mapFromRequest(id, req);
        store.put(id, book);
        return book;
    }

    public Optional<Book> update(Integer id, BookRequest req) {
        if (!store.containsKey(id)) return Optional.empty();
        Book book = mapFromRequest(id, req);
        store.put(id, book);
        return Optional.of(book);
    }

    public boolean delete(Integer id) {
        return store.remove(id) != null;
    }

    public long count() {
        return store.size();
    }

    // ── Mapper ────────────────────────────────────────────────
    private Book mapFromRequest(Integer id, BookRequest req) {
        return new Book(
                id,
                req.getTitle(),
                req.getAuthor(),
                req.getDescription(),
                req.getCoverUrl()
        );
    }
}