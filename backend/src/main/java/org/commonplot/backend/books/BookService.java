package org.commonplot.backend.books;

import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// ============================================================
//  BookService.java — CRUD cu JPA repository
//  ConcurrentHashMap → BookRepository (PostgreSQL)
//  ID-urile sunt generate de DB (SERIAL), nu de AtomicInteger
//  Mock data e în V1__init.sql (Flyway), nu aici
// ============================================================
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════════

    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> getById(Integer id) {
        return bookRepository.findById(id);
    }

    public Book create(BookRequest req) {
        return bookRepository.save(mapFromRequest(req));
    }

    public Optional<Book> update(Integer id, BookRequest req) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(req.getTitle());
            existing.setAuthor(req.getAuthor());
            existing.setDescription(req.getDescription());
            existing.setCoverUrl(req.getCoverUrl());
            return bookRepository.save(existing);
        });
    }

    public boolean delete(Integer id) {
        if (!bookRepository.existsById(id)) return false;
        bookRepository.deleteById(id);
        return true;
    }

    public long count() {
        return bookRepository.count();
    }

    // ── Mapper ────────────────────────────────────────────────
    private Book mapFromRequest(BookRequest req) {
        return new Book(
                req.getTitle(),
                req.getAuthor(),
                req.getDescription(),
                req.getCoverUrl()
        );
    }
}