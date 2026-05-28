package org.commonplot.backend.meetupsTest;

import org.commonplot.backend.books.BookRepository;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.meetups.MeetupRepository;
import org.commonplot.backend.meetups.MeetupService;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.meetups.model.MeetupRequest;
import org.commonplot.backend.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ============================================================
//  MeetupServiceTest.java — unit tests pentru MeetupService
//  Folosește mock-uri pentru repository-uri (fără DB reală)
//  Profilul de test: H2 in-memory
// ============================================================
class MeetupServiceTest {

    private MeetupService service;
    private MeetupRepository meetupRepository;
    private BookRepository bookRepository;

    // ── Sample data ───────────────────────────────────────────
    private Book sampleBook() {
        Book b = new Book("Atomic Habits", "James Clear", "Build good habits.", null);
        b.setId(1);
        return b;
    }

    private Meetup sampleMeetup(Long id) {
        Meetup m = new Meetup(
                "Test Book Club", "Test Cafe, Cluj", "2026-12-01T10:00",
                sampleBook(), 1, "testuser", 90, 4.5, "A test meetup."
        );
        m.setId(id);
        return m;
    }

    private MeetupRequest validRequest() {
        MeetupRequest req = new MeetupRequest();
        req.setTitleEvent("Test Book Club");
        req.setLocation("Test Cafe, Cluj");
        req.setDate("2026-12-01T10:00");
        req.setBookID(1);
        req.setOwnerID(1);
        req.setOwnerUsername("testuser");
        req.setDuration(90);
        req.setRating(4.5);
        req.setDescription("A test meetup.");
        return req;
    }

    @BeforeEach
    void setUp() {
        meetupRepository = Mockito.mock(MeetupRepository.class);
        bookRepository   = Mockito.mock(BookRepository.class);
        SimpMessagingTemplate mockTemplate = Mockito.mock(SimpMessagingTemplate.class);

        service = new MeetupService(meetupRepository, bookRepository, mockTemplate);

        // Default mock behavior
        when(bookRepository.findById(1)).thenReturn(Optional.of(sampleBook()));
    }

    // ── CREATE ────────────────────────────────────────────────

    @Test
    void create_returnsNewMeetupWithId() {
        Meetup saved = sampleMeetup(1L);
        when(meetupRepository.save(any())).thenReturn(saved);

        Meetup result = service.create(validRequest());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitleEvent()).isEqualTo("Test Book Club");
    }

    @Test
    void create_throwsWhenBookNotFound() {
        when(bookRepository.findById(99)).thenReturn(Optional.empty());
        MeetupRequest req = validRequest();
        req.setBookID(99);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book with ID 99 not found");
    }

    @Test
    void create_setsBookOnMeetup() {
        Meetup saved = sampleMeetup(1L);
        when(meetupRepository.save(any())).thenReturn(saved);

        Meetup result = service.create(validRequest());

        assertThat(result.getBook()).isNotNull();
        assertThat(result.getBookTitle()).isEqualTo("Atomic Habits");
    }

    // ── READ ──────────────────────────────────────────────────

    @Test
    void getById_returnsExistingMeetup() {
        when(meetupRepository.findById(1L)).thenReturn(Optional.of(sampleMeetup(1L)));

        Optional<Meetup> result = service.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitleEvent()).isEqualTo("Test Book Club");
    }

    @Test
    void getById_returnsEmptyForNonexistentId() {
        when(meetupRepository.findById(99999L)).thenReturn(Optional.empty());

        assertThat(service.getById(99999L)).isEmpty();
    }

    @Test
    void getAll_returnsPaginatedContent() {
        List<Meetup> meetups = List.of(sampleMeetup(1L), sampleMeetup(2L));
        Page<Meetup> page = new PageImpl<>(meetups, PageRequest.of(0, 10), 2);
        when(meetupRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<Meetup> result = service.getAll(0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
    }

    @Test
    void getAll_emptyPage() {
        Page<Meetup> emptyPage = new PageImpl<>(List.of(), PageRequest.of(99, 10), 0);
        when(meetupRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PagedResponse<Meetup> result = service.getAll(99, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Test
    void update_updatesExistingMeetup() {
        Meetup existing = sampleMeetup(1L);
        when(meetupRepository.findById(1L)).thenReturn(Optional.of(existing));

        MeetupRequest req = validRequest();
        req.setLocation("New Location, Iași");
        Meetup updated = sampleMeetup(1L);
        updated.setLocation("New Location, Iași");
        when(meetupRepository.save(any())).thenReturn(updated);

        Optional<Meetup> result = service.update(1L, req);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo("New Location, Iași");
    }

    @Test
    void update_returnsEmptyForNonexistentId() {
        when(meetupRepository.findById(99999L)).thenReturn(Optional.empty());

        assertThat(service.update(99999L, validRequest())).isEmpty();
    }

    @Test
    void update_preservesId() {
        Meetup existing = sampleMeetup(1L);
        when(meetupRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(meetupRepository.save(any())).thenReturn(existing);

        Optional<Meetup> result = service.update(1L, validRequest());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    // ── DELETE ────────────────────────────────────────────────

    @Test
    void delete_returnsTrueWhenExists() {
        when(meetupRepository.existsById(1L)).thenReturn(true);

        boolean result = service.delete(1L);

        assertThat(result).isTrue();
        verify(meetupRepository).deleteById(1L);
    }

    @Test
    void delete_returnsFalseWhenNotExists() {
        when(meetupRepository.existsById(99999L)).thenReturn(false);

        assertThat(service.delete(99999L)).isFalse();
        verify(meetupRepository, never()).deleteById(any());
    }

    // ── STATISTICS ────────────────────────────────────────────

    @Test
    void count_delegatesToRepository() {
        when(meetupRepository.count()).thenReturn(42L);

        assertThat(service.count()).isEqualTo(42L);
    }

    @Test
    void averageRating_returnsRepositoryValue() {
        when(meetupRepository.findAverageRating()).thenReturn(4.3);

        assertThat(service.averageRating()).isEqualTo(4.3);
    }

    @Test
    void averageRating_returnsZeroWhenNull() {
        when(meetupRepository.findAverageRating()).thenReturn(null);

        assertThat(service.averageRating()).isEqualTo(0.0);
    }

    @Test
    void statsByLocation_returnsMap() {
        when(meetupRepository.countByLocation()).thenReturn(
                List.of(new Object[]{"Cluj", 5L}, new Object[]{"București", 3L})
        );

        var result = service.statsByLocation();

        assertThat(result).containsEntry("Cluj", 5L);
        assertThat(result).containsEntry("București", 3L);
    }

    // ── RELAȚIE 1-to-many ─────────────────────────────────────

    @Test
    void getMeetupsByBookId_returnsCorrectMeetups() {
        List<Meetup> bookMeetups = List.of(sampleMeetup(1L), sampleMeetup(2L));
        when(meetupRepository.findByBookId(1)).thenReturn(bookMeetups);

        List<Meetup> result = service.getMeetupsByBookId(1);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(m -> m.getBookID().equals(1));
    }

    @Test
    void getMeetupsByBookId_returnsEmptyForUnknownBook() {
        when(meetupRepository.findByBookId(999)).thenReturn(List.of());

        assertThat(service.getMeetupsByBookId(999)).isEmpty();
    }

    // ── GENERATOR ─────────────────────────────────────────────

    @Test
    void generator_startsAndStops() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook()));

        assertThat(service.isGeneratorRunning()).isFalse();
        service.startGenerator(60);
        assertThat(service.isGeneratorRunning()).isTrue();
        service.stopGenerator();
        assertThat(service.isGeneratorRunning()).isFalse();
    }

    @Test
    void generator_startTwiceDoesNotDuplicate() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook()));

        service.startGenerator(60);
        service.startGenerator(60);
        assertThat(service.isGeneratorRunning()).isTrue();
        service.stopGenerator();
    }
}