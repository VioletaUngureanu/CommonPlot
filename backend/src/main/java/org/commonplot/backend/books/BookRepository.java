package org.commonplot.backend.books;

import org.commonplot.backend.books.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ============================================================
//  BookRepository — Spring Data JPA
//  Toate operațiunile CRUD sunt generate automat.
//  Metodele custom urmează convenția Spring Data (findBy...)
// ============================================================
@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    // Caută cărți după autor
    List<Book> findByAuthorContainingIgnoreCase(String author);

    // Caută cărți după titlu
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Verifică dacă există o carte cu același titlu și autor
    boolean existsByTitleAndAuthor(String title, String author);
}