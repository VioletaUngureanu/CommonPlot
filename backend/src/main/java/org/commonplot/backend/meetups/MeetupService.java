package org.commonplot.backend.meetups;


import net.datafaker.Faker;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.meetups.model.MeetupRequest;
import org.commonplot.backend.PagedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Service
public class MeetupService {

    // ── RAM Storage ───────────────────────────────────────────
    // ConcurrentHashMap = thread-safe, fără DB, fără persistență
    private final ConcurrentHashMap<Long, Meetup> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(200);

    // ── Faker + Generator (Silver) ────────────────────────────
    private final Faker faker = new Faker();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> generatorTask = null;
    private volatile boolean generatorRunning = false;

    // ── WebSocket (Silver) ────────────────────────────────────
    private final SimpMessagingTemplate messagingTemplate;

    // ── Date mock inițiale ────────────────────────────────────
    private static final List<String[]> BOOKS = List.of(
            new String[]{"1",  "Crime and Punishment",    "Fyodor Dostoevsky"},
            new String[]{"2",  "Atomic Habits",           "James Clear"},
            new String[]{"3", "The Trial",               "Franz Kafka"},
            new String[]{"4", "War and Peace",           "Leo Tolstoy"},
            new String[]{"5", "The Brothers Karamazov",  "Fyodor Dostoevsky"}
    );

    private static final List<String> LOCATIONS = List.of(
            "Bunt, Cluj-Napoca", "Cafe, Iași", "Everast, București",
            "Hub, București", "Meron, Cluj-Napoca"
    );

    @Autowired
    public MeetupService(@Nullable SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        initMockData();
    }

    // ── Init mock data ────────────────────────────────────────
    private void initMockData() {
        createInternal("Morning Coffee & Dostoievski", "Bunt, Cluj-Napoca",
                "2026-05-12T10:30", 1, "The Brothers Karamazov", "Fyodor Dostoevsky",
                53, "Alex M.", 120, 4.9, "Discussing moral dilemmas from the first chapters.");
        createInternal("Atomic Habits Monday", "Cafe, Iași",
                "2026-04-06T15:00", 2, "Atomic Habits", "James Clear",
                34, "Maria P.", 90, 4.5, "Building better reading habits together.");
        createInternal("Evening Philosophy", "Everast, București",
                "2026-04-06T18:00", 3, "Crime and Punishment", "Fyodor Dostoevsky",
                70, "Ionut B.", 60, 4.2, "A short but intense discussion about existentialism.");
        createInternal("Kafka & Cappuccino", "Meron, Cluj-Napoca",
                "2026-04-07T10:00", 4, "The Trial", "Franz Kafka",
                136, "Andrei V.", 120, 4.8, "Exploring the absurd through Kafka's lens.");
        createInternal("War and Peace Tuesday", "Everast, București",
                "2026-04-07T19:30", 5, "War and Peace", "Leo Tolstoy",
                91, "Ioana L.", 90, 4.6, "Discussing the Napoleonic campaigns.");
    }

    private void createInternal(String title, String location, String date,
                                int bookId, String bookTitle, String bookAuthor,
                                int ownerID, String username, int duration,
                                double rating, String desc) {
        Long id = idCounter.getAndIncrement();
        store.put(id, new Meetup(id, title, location, date,
                bookId, bookTitle, bookAuthor, ownerID, username, duration, rating, desc));
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD operations
    // ══════════════════════════════════════════════════════════

    /** GET all — paginat server-side */
    public PagedResponse<Meetup> getAll(int page, int pageSize) {
        List<Meetup> all = new ArrayList<>(store.values());
        // Sortare după ID pentru consistență
        all.sort(Comparator.comparing(Meetup::getId));

        long total = all.size();
        int start  = page * pageSize;
        int end    = Math.min(start + pageSize, all.size());

        List<Meetup> pageContent = start >= all.size()
                ? Collections.emptyList()
                : all.subList(start, end);

        return new PagedResponse<>(pageContent, page, pageSize, total);
    }

    /** GET by ID */
    public Optional<Meetup> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /** POST — creare */
    public Meetup create(MeetupRequest req) {
        Long id = idCounter.getAndIncrement();
        Meetup m = mapFromRequest(id, req);
        store.put(id, m);
        return m;
    }

    /** PUT — actualizare completă */
    public Optional<Meetup> update(Long id, MeetupRequest req) {
        if (!store.containsKey(id)) return Optional.empty();
        Meetup m = mapFromRequest(id, req);
        store.put(id, m);
        return Optional.of(m);
    }

    /** DELETE */
    public boolean delete(Long id) {
        return store.remove(id) != null;
    }

    /** Număr total de meetup-uri */
    public long count() {
        return store.size();
    }

    // ── Statistics ────────────────────────────────────────────

    /** Statistici per locație */
    public Map<String, Long> statsByLocation() {
        return store.values().stream()
                .collect(Collectors.groupingBy(Meetup::getLocation, Collectors.counting()));
    }

    /** Statistici per carte */
    public Map<String, Long> statsByBook() {
        return store.values().stream()
                .filter(m -> m.getBookTitle() != null)
                .collect(Collectors.groupingBy(Meetup::getBookTitle, Collectors.counting()));
    }


    /** Rating mediu */
    public double averageRating() {
        return store.values().stream()
                .filter(m -> m.getRating() != null)
                .mapToDouble(Meetup::getRating)
                .average()
                .orElse(0.0);
    }

    public List<Meetup> getMeetupsByBookId(Integer bookId) {
        return store.values().stream()
                .filter(m -> m.getBookID() != null && bookId.equals(m.getBookID()))
                .sorted(Comparator.comparing(Meetup::getId))
                .collect(Collectors.toList());
    }
    // ══════════════════════════════════════════════════════════
    //  Silver: Auto-generator cu Faker + WebSocket
    // ══════════════════════════════════════════════════════════

    public boolean isGeneratorRunning() {
        return generatorRunning;
    }

    /** Pornește generatorul automat */
    public void startGenerator(int intervalSeconds) {
        if (generatorRunning) return;
        generatorRunning = true;
        generatorTask = scheduler.scheduleAtFixedRate(
                this::generateAndNotify,
                0,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }

    /** Oprește generatorul */
    public void stopGenerator() {
        if (generatorTask != null) {
            generatorTask.cancel(false);
            generatorTask = null;
        }
        generatorRunning = false;
    }

    /** Generează un meetup fals și notifică clienții prin WebSocket */
    private void generateAndNotify() {
        String[] book = BOOKS.get(faker.number().numberBetween(0, BOOKS.size()));
        String location = LOCATIONS.get(faker.number().numberBetween(0, LOCATIONS.size()));

        // Dată viitoare aleatoare (1-30 zile)
        LocalDateTime futureDate = LocalDateTime.now()
                .plusDays(faker.number().numberBetween(1, 30))
                .withHour(faker.number().numberBetween(8, 20))
                .withMinute(0).withSecond(0).withNano(0);

        String dateStr = futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        Long id = idCounter.getAndIncrement();
        Meetup generated = new Meetup(
                id,
                "Auto: " + book[1] + " @ " + location.split(",")[0],
                location,
                dateStr,
                Integer.parseInt(book[0]),
                book[1],
                book[2],
                faker.number().numberBetween(1, 200),
                faker.name().firstName() + " " + faker.name().lastName().charAt(0) + ".",
                List.of(60, 90, 120).get(faker.number().numberBetween(0, 3)),
                Math.round((3.5 + faker.number().randomDouble(1, 0, 15) / 10) * 10.0) / 10.0,
                faker.lorem().sentence(10)
        );

        store.put(id, generated);

        // Notifică toți clienții WebSocket conectați
        messagingTemplate.convertAndSend("/topic/meetups", generated);
    }

    // ── Mapper ────────────────────────────────────────────────
    private Meetup mapFromRequest(Long id, MeetupRequest req) {
        return new Meetup(
                id,
                req.getTitleEvent(),
                req.getLocation(),
                req.getDate(),
                req.getBookID(),
                req.getBookTitle(),
                req.getBookAuthor(),
                req.getOwnerID(),
                req.getOwnerUsername(),
                req.getDuration(),
                req.getRating(),
                req.getDescription()
        );
    }
}