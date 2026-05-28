package org.commonplot.backend.meetups;

import net.datafaker.Faker;
import org.commonplot.backend.PagedResponse;
import org.commonplot.backend.books.BookRepository;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.meetups.model.MeetupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

// ============================================================
//  MeetupService.java — CRUD cu JPA repository
//  ConcurrentHashMap → MeetupRepository (PostgreSQL)
//  Generator Faker + WebSocket păstrate (Silver)
// ============================================================
@Service
public class MeetupService {

    private final MeetupRepository  meetupRepository;
    private final BookRepository    bookRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Faker + Generator (Silver) ────────────────────────────
    private final Faker faker = new Faker();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> generatorTask  = null;
    private volatile boolean generatorRunning = false;

    // ID-uri de cărți din DB — populat la prima generare
    private List<Integer> bookIds = new ArrayList<>();

    private static final List<String> LOCATIONS = List.of(
            "Bunt, Cluj-Napoca", "Cafe, Iași", "Everast, București",
            "Hub, București", "Meron, Cluj-Napoca"
    );

    public MeetupService(MeetupRepository meetupRepository,
                         BookRepository bookRepository,
                         @Nullable SimpMessagingTemplate messagingTemplate) {
        this.meetupRepository  = meetupRepository;
        this.bookRepository    = bookRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════════

    /** GET all — paginare server-side cu Spring Data */
    public PagedResponse<Meetup> getAll(int page, int pageSize) {
        Page<Meetup> result = meetupRepository.findAll(
                PageRequest.of(page, pageSize, Sort.by("id"))
        );
        return new PagedResponse<>(
                result.getContent(),
                page,
                pageSize,
                result.getTotalElements()
        );
    }

    /** GET by ID */
    public Optional<Meetup> getById(Long id) {
        return meetupRepository.findById(id);
    }

    /** POST — creare */
    public Meetup create(MeetupRequest req) {
        Book book = bookRepository.findById(req.getBookID())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Book with ID " + req.getBookID() + " not found."
                ));
        Meetup m = mapFromRequest(req, book);
        return meetupRepository.save(m);
    }

    /** PUT — actualizare */
    public Optional<Meetup> update(Long id, MeetupRequest req) {
        return meetupRepository.findById(id).map(existing -> {
            Book book = bookRepository.findById(req.getBookID())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Book with ID " + req.getBookID() + " not found."
                    ));
            existing.setTitleEvent(req.getTitleEvent());
            existing.setLocation(req.getLocation());
            existing.setDate(req.getDate());
            existing.setBook(book);
            existing.setOwnerID(req.getOwnerID());
            existing.setOwnerUsername(req.getOwnerUsername());
            existing.setDuration(req.getDuration());
            existing.setRating(req.getRating());
            existing.setDescription(req.getDescription());
            return meetupRepository.save(existing);
        });
    }

    /** DELETE */
    public boolean delete(Long id) {
        if (!meetupRepository.existsById(id)) return false;
        meetupRepository.deleteById(id);
        return true;
    }

    /** Total */
    public long count() {
        return meetupRepository.count();
    }

    // ── Statistics ────────────────────────────────────────────

    public Map<String, Long> statsByLocation() {
        return meetupRepository.countByLocation().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long)   row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Long> statsByBook() {
        return meetupRepository.countByBook().stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long)   row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public double averageRating() {
        Double avg = meetupRepository.findAverageRating();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // ── Relație 1-to-many ─────────────────────────────────────
    public List<Meetup> getMeetupsByBookId(Integer bookId) {
        return meetupRepository.findByBookId(bookId);
    }

    // ══════════════════════════════════════════════════════════
    //  Silver: Generator Faker + WebSocket
    // ══════════════════════════════════════════════════════════

    public boolean isGeneratorRunning() {
        return generatorRunning;
    }

    public void startGenerator(int intervalSeconds) {
        if (generatorRunning) return;
        generatorRunning = true;
        // Încarcă ID-urile cărților din DB
        bookIds = bookRepository.findAll().stream()
                .map(Book::getId)
                .collect(Collectors.toList());
        generatorTask = scheduler.scheduleAtFixedRate(
                this::generateAndNotify, 0, intervalSeconds, TimeUnit.SECONDS
        );
    }

    public void stopGenerator() {
        if (generatorTask != null) {
            generatorTask.cancel(false);
            generatorTask = null;
        }
        generatorRunning = false;
    }

    private void generateAndNotify() {
        if (bookIds.isEmpty()) return;

        Integer bookId = bookIds.get(faker.number().numberBetween(0, bookIds.size()));
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) return;

        String location = LOCATIONS.get(faker.number().numberBetween(0, LOCATIONS.size()));

        LocalDateTime futureDate = LocalDateTime.now()
                .plusDays(faker.number().numberBetween(1, 30))
                .withHour(faker.number().numberBetween(8, 20))
                .withMinute(0).withSecond(0).withNano(0);

        String dateStr = futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        Meetup generated = new Meetup(
                "Auto: " + book.getTitle() + " @ " + location.split(",")[0],
                location,
                dateStr,
                book,
                faker.number().numberBetween(1, 200),
                faker.name().firstName() + " " + faker.name().lastName().charAt(0) + ".",
                List.of(60, 90, 120).get(faker.number().numberBetween(0, 3)),
                Math.round((3.5 + faker.number().randomDouble(1, 0, 15) / 10) * 10.0) / 10.0,
                faker.lorem().sentence(10)
        );

        Meetup saved = meetupRepository.save(generated);

        if (messagingTemplate != null) {
            messagingTemplate.convertAndSend("/topic/meetups", saved);
        }
    }

    // ── Mapper ────────────────────────────────────────────────
    private Meetup mapFromRequest(MeetupRequest req, Book book) {
        return new Meetup(
                req.getTitleEvent(),
                req.getLocation(),
                req.getDate(),
                book,
                req.getOwnerID(),
                req.getOwnerUsername(),
                req.getDuration(),
                req.getRating(),
                req.getDescription()
        );
    }
}