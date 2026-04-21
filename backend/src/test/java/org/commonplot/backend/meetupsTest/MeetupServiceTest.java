package org.commonplot.backend.meetupsTest;



import org.commonplot.backend.meetups.MeetupService;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.meetups.model.MeetupRequest;
import org.commonplot.backend.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

// ============================================================
//  MeetupServiceTest.java — unit tests pentru service
//  Testează CRUD, paginare și statistici
//  Coverage maxim pe logica de business
// ============================================================
class MeetupServiceTest {

    private MeetupService service;

    // Helper: request valid
    private MeetupRequest validRequest() {
        MeetupRequest req = new MeetupRequest();
        req.setTitleEvent("Test Book Club");
        req.setLocation("Test Cafe, Cluj");
        req.setDate("2026-12-01T10:00");
        req.setBookID(57);
        req.setBookTitle("Atomic Habits");
        req.setBookAuthor("James Clear");
        req.setOwnerID(1);
        req.setOwnerUsername("testuser");
        req.setDuration(90);
        req.setRating(4.5);
        req.setDescription("A test meetup.");
        return req;
    }

    @BeforeEach
    void setUp() {
        // Mock WebSocket template — nu trimitem mesaje reale în teste
        SimpMessagingTemplate mockTemplate = Mockito.mock(SimpMessagingTemplate.class);
        service = new MeetupService(mockTemplate);
    }

    // ── CREATE ────────────────────────────────────────────────

    @Test
    void create_returnsNewMeetupWithId() {
        Meetup m = service.create(validRequest());
        assertThat(m).isNotNull();
        assertThat(m.getId()).isNotNull();
        assertThat(m.getTitleEvent()).isEqualTo("Test Book Club");
    }

    @Test
    void create_twoMeetupsHaveDifferentIds() {
        Meetup m1 = service.create(validRequest());
        Meetup m2 = service.create(validRequest());
        assertThat(m1.getId()).isNotEqualTo(m2.getId());
    }

    @Test
    void create_increasesCount() {
        long before = service.count();
        service.create(validRequest());
        assertThat(service.count()).isEqualTo(before + 1);
    }

    // ── READ ──────────────────────────────────────────────────

    @Test
    void getById_returnsExistingMeetup() {
        Meetup created = service.create(validRequest());
        Optional<Meetup> found = service.getById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitleEvent()).isEqualTo("Test Book Club");
    }

    @Test
    void getById_returnsEmptyForNonexistentId() {
        assertThat(service.getById(99999L)).isEmpty();
    }

    @Test
    void getAll_returnsMockDataInitially() {
        PagedResponse<Meetup> page = service.getAll(0, 10);
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getTotalElements()).isGreaterThan(0);
    }

    // ── PAGINATION ────────────────────────────────────────────

    @Test
    void getAll_paginationReturnsCorrectPageSize() {
        // Adaugă suficiente meetup-uri
        for (int i = 0; i < 10; i++) service.create(validRequest());

        PagedResponse<Meetup> page = service.getAll(0, 3);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getPageSize()).isEqualTo(3);
    }

    @Test
    void getAll_page2HasDifferentContentThanPage1() {
        for (int i = 0; i < 10; i++) service.create(validRequest());

        PagedResponse<Meetup> page1 = service.getAll(0, 3);
        PagedResponse<Meetup> page2 = service.getAll(1, 3);

        assertThat(page1.getContent().get(0).getId())
                .isNotEqualTo(page2.getContent().get(0).getId());
    }

    @Test
    void getAll_emptyPageBeyondTotal() {
        PagedResponse<Meetup> page = service.getAll(999, 10);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void getAll_totalPagesCalculatedCorrectly() {
        long total = service.count();
        PagedResponse<Meetup> page = service.getAll(0, 3);
        int expectedPages = (int) Math.ceil((double) total / 3);
        assertThat(page.getTotalPages()).isEqualTo(expectedPages);
    }

    @Test
    void getAll_firstPageIsFirst() {
        PagedResponse<Meetup> page = service.getAll(0, 5);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.isLast()).isTrue();
    }

    // ── UPDATE ────────────────────────────────────────────────

    @Test
    void update_updatesExistingMeetup() {
        Meetup created = service.create(validRequest());
        MeetupRequest updated = validRequest();
        updated.setLocation("New Location, Iași");

        Optional<Meetup> result = service.update(created.getId(), updated);

        assertThat(result).isPresent();
        assertThat(result.get().getLocation()).isEqualTo("New Location, Iași");
    }

    @Test
    void update_preservesIdAfterUpdate() {
        Meetup created = service.create(validRequest());
        service.update(created.getId(), validRequest());

        Optional<Meetup> found = service.getById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
    }

    @Test
    void update_returnsEmptyForNonexistentId() {
        Optional<Meetup> result = service.update(99999L, validRequest());
        assertThat(result).isEmpty();
    }

    @Test
    void update_doesNotChangeCount() {
        Meetup created = service.create(validRequest());
        long before = service.count();
        service.update(created.getId(), validRequest());
        assertThat(service.count()).isEqualTo(before);
    }

    // ── DELETE ────────────────────────────────────────────────

    @Test
    void delete_removesExistingMeetup() {
        Meetup created = service.create(validRequest());
        long before = service.count();

        boolean deleted = service.delete(created.getId());

        assertThat(deleted).isTrue();
        assertThat(service.count()).isEqualTo(before - 1);
        assertThat(service.getById(created.getId())).isEmpty();
    }

    @Test
    void delete_returnsFalseForNonexistentId() {
        assertThat(service.delete(99999L)).isFalse();
    }

    @Test
    void delete_doesNotAffectOtherMeetups() {
        Meetup m1 = service.create(validRequest());
        Meetup m2 = service.create(validRequest());

        service.delete(m1.getId());

        assertThat(service.getById(m2.getId())).isPresent();
    }

    // ── STATISTICS ────────────────────────────────────────────

    @Test
    void statsByLocation_returnsNonEmptyMap() {
        assertThat(service.statsByLocation()).isNotEmpty();
    }

    @Test
    void statsByBook_returnsNonEmptyMap() {
        assertThat(service.statsByBook()).isNotEmpty();
    }

    @Test
    void averageRating_returnsPositiveValue() {
        assertThat(service.averageRating()).isGreaterThan(0.0);
    }

    @Test
    void count_reflectsTotalMeetups() {
        long before = service.count();
        service.create(validRequest());
        assertThat(service.count()).isEqualTo(before + 1);
    }

    // ── GENERATOR ─────────────────────────────────────────────

    @Test
    void generator_startsAndStops() throws InterruptedException {
        assertThat(service.isGeneratorRunning()).isFalse();

        service.startGenerator(1);
        assertThat(service.isGeneratorRunning()).isTrue();

        // Așteptăm 2s să genereze cel puțin un meetup
        Thread.sleep(2000);
        long countAfter = service.count();
        assertThat(countAfter).isGreaterThan(5); // erau 5 mock inițiale

        service.stopGenerator();
        assertThat(service.isGeneratorRunning()).isFalse();
    }

    @Test
    void generator_startTwiceDoesNotDuplicate() {
        service.startGenerator(10);
        service.startGenerator(10); // al doilea apel e ignorat
        assertThat(service.isGeneratorRunning()).isTrue();
        service.stopGenerator();
    }
}