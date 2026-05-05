package org.commonplot.backend.meetups;

import org.commonplot.backend.meetups.model.Meetup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// ============================================================
//  MeetupRepository — Spring Data JPA
//  Paginare, filtrare, statistici prin metode declarative
//  și @Query JPQL pentru statistici mai complexe
// ============================================================
@Repository
public interface MeetupRepository extends JpaRepository<Meetup, Long> {

    // ── Relație 1-to-many: meetup-urile unei cărți ────────────
    List<Meetup> findByBookId(Integer bookId);

    Page<Meetup> findByBookId(Integer bookId, Pageable pageable);

    // ── Filtrare ──────────────────────────────────────────────
    List<Meetup> findByLocation(String location);

    List<Meetup> findByOwnerID(Integer ownerID);

    List<Meetup> findByLocationContainingIgnoreCase(String location);

    // ── Statistici prin JPQL ──────────────────────────────────

    // Rating mediu global
    @Query("SELECT AVG(m.rating) FROM Meetup m WHERE m.rating IS NOT NULL")
    Double findAverageRating();

    // Număr meetup-uri per locație
    @Query("SELECT m.location, COUNT(m) FROM Meetup m GROUP BY m.location ORDER BY COUNT(m) DESC")
    List<Object[]> countByLocation();

    // Număr meetup-uri per carte
    @Query("SELECT m.book.title, COUNT(m) FROM Meetup m GROUP BY m.book.title ORDER BY COUNT(m) DESC")
    List<Object[]> countByBook();

    // Meetup-uri cu rating mai mare decât o valoare
    List<Meetup> findByRatingGreaterThanEqual(Double rating);

    // Total meetup-uri pentru o carte
    long countByBookId(Integer bookId);
}