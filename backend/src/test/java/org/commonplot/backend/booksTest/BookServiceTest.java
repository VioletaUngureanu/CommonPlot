package org.commonplot.backend.booksTest;

import org.commonplot.backend.books.BookRepository;
import org.commonplot.backend.books.BookService;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.books.model.BookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// ============================================================
//  BookServiceTest.java — unit tests pentru BookService
//  Repository-ul e mock-uit — nu e nevoie de DB reală
// ============================================================
class BookServiceTest {

    private BookService service;
    private BookRepository bookRepository;

    private Book sampleBook(Integer id) {
        Book b = new Book("Atomic Habits", "James Clear",
                "Build good habits.", "https://example.com/cover.jpg");
        b.setId(id);
        return b;
    }

    private BookRequest validRequest() {
        BookRequest req = new BookRequest();
        req.setTitle("Atomic Habits");
        req.setAuthor("James Clear");
        req.setDescription("Build good habits.");
        req.setCoverUrl("https://example.com/cover.jpg");
        return req;
    }

    @BeforeEach
    void setUp() {
        bookRepository = Mockito.mock(BookRepository.class);
        service = new BookService(bookRepository);
    }

    // ── CREATE ────────────────────────────────────────────────

    @Test
    void create_returnsBookWithDataFromRequest() {
        Book saved = sampleBook(1);
        when(bookRepository.save(any())).thenReturn(saved);

        Book result = service.create(validRequest());

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Atomic Habits");
        assertThat(result.getAuthor()).isEqualTo("James Clear");
    }

    @Test
    void create_callsSave() {
        when(bookRepository.save(any())).thenReturn(sampleBook(1));

        service.create(validRequest());

        verify(bookRepository).save(any(Book.class));
    }

    // ── READ ──────────────────────────────────────────────────

    @Test
    void getAll_returnsAllBooks() {
        List<Book> books = List.of(sampleBook(1), sampleBook(2));
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = service.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void getAll_returnsEmptyListWhenNoBooksExist() {
        when(bookRepository.findAll()).thenReturn(List.of());

        assertThat(service.getAll()).isEmpty();
    }

    @Test
    void getById_returnsBookWhenFound() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook(1)));

        Optional<Book> result = service.getById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1);
        assertThat(result.get().getTitle()).isEqualTo("Atomic Habits");
    }

    @Test
    void getById_returnsEmptyWhenNotFound() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        assertThat(service.getById(999)).isEmpty();
    }

    @Test
    void count_returnsRepositoryCount() {
        when(bookRepository.count()).thenReturn(5L);

        assertThat(service.count()).isEqualTo(5L);
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Test
    void update_updatesExistingBook() {
        Book existing = sampleBook(1);
        when(bookRepository.findById(1)).thenReturn(Optional.of(existing));

        BookRequest req = validRequest();
        req.setTitle("Updated Title");
        Book updated = new Book("Updated Title", "James Clear", "desc", null);
        updated.setId(1);
        when(bookRepository.save(any())).thenReturn(updated);

        Optional<Book> result = service.update(1, req);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Updated Title");
    }

    @Test
    void update_returnsEmptyWhenBookNotFound() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        assertThat(service.update(999, validRequest())).isEmpty();
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_callsSaveOnExistingBook() {
        Book existing = sampleBook(1);
        when(bookRepository.findById(1)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any())).thenReturn(existing);

        service.update(1, validRequest());

        verify(bookRepository).save(existing);
    }

    @Test
    void update_preservesId() {
        Book existing = sampleBook(1);
        when(bookRepository.findById(1)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any())).thenReturn(existing);

        Optional<Book> result = service.update(1, validRequest());

        assertThat(result.get().getId()).isEqualTo(1);
    }

    // ── DELETE ────────────────────────────────────────────────

    @Test
    void delete_returnsTrueWhenBookExists() {
        when(bookRepository.existsById(1)).thenReturn(true);

        boolean result = service.delete(1);

        assertThat(result).isTrue();
        verify(bookRepository).deleteById(1);
    }

    @Test
    void delete_returnsFalseWhenBookNotFound() {
        when(bookRepository.existsById(999)).thenReturn(false);

        assertThat(service.delete(999)).isFalse();
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void delete_callsDeleteByIdOnlyWhenExists() {
        when(bookRepository.existsById(1)).thenReturn(true);
        service.delete(1);
        verify(bookRepository, times(1)).deleteById(1);
    }
}