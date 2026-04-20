package org.commonplot.backend.books;

import net.datafaker.Faker;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.PagedResponse;
import org.commonplot.backend.books.model.BookRequest;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.meetups.model.MeetupRequest;
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
public class BookService {
    private final ConcurrentHashMap<Long, Book> store = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    private final Faker faker = new Faker();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> generatorTask = null;
    private volatile boolean generatorRunning = false;

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public BookService(@Nullable SimpMessagingTemplate messagingTemplate)
    {
        this.messagingTemplate = messagingTemplate;
    }

    private void initMockData(){
        createInternal( "Crime and Punishment",    "Fyodor Dostoevsky", "Raskolnikov, a destitute and desperate former student, wanders through the slums of St Petersburg and commits a random murder without remorse or regret. He imagines himself to be a great man, a Napoleon: acting for a higher purpose beyond conventional moral law. But as he embarks on a dangerous game of cat-and-mouse with Porfiry, a suspicious detective, Raskolnikov is pursued by the growing voice of his conscience and finds the noose of his own guilt tightening around his neck. Only Sonya, a downtrodden prostitute, can offer the chance of redemption. As the ensuing investigation and trial reveal the true identity of the murderer, Dostoyevsky's dark masterpiece evokes a world where the lines between innocence and corruption, good and evil, blur and everyone's faith in humanity is tested.");
        createInternal("Atomic Habits",           "James Clear", "No matter your goals, Atomic Habits offers a proven framework for improving—every day. James Clear, one of the world's leading experts on habit formation, reveals practical strategies that will teach you exactly how to form good habits, break bad ones, and master the tiny behaviors that lead to remarkable results." );
        createInternal( "The Trial",               "Franz Kafka", "The Trial is the terrifying tale of Josef K., a respectable bank officer who is suddenly and inexplicably arrested and must defend himself against a charge about which he can get no information. Whether read as an existential tale, a parable, or a prophecy of the excesses of modern bureaucracy wedded to the madness of totalitarianism, The Trial has resonated with chilling truth for generations of readers.");
        createInternal("War and Peace",           "Leo Tolstoy", "War and Peace broadly focuses on Napoleon’s invasion of Russia in 1812 and follows three of the most well-known characters in literature: Pierre Bezukhov, the illegitimate son of a count who is fighting for his inheritance and yearning for spiritual fulfillment; Prince Andrei Bolkonsky, who leaves his family behind to fight in the war against Napoleon; and Natasha Rostov, the beautiful young daughter of a nobleman who intrigues both men." );
        createInternal("The Brothers Karamazov",  "Fyodor Dostoevsky", "The Brothers Karamazov is a murder mystery, a courtroom drama, and an exploration of erotic rivalry in a series of triangular love affairs involving the “wicked and sentimental” Fyodor Pavlovich Karamazov and his three sons―the impulsive and sensual Dmitri; the coldly rational Ivan; and the healthy, red-cheeked young novice Alyosha. Through the gripping events of their story, Dostoevsky portrays the whole of Russian life, is social and spiritual striving, in what was both the golden age and a tragic turning point in Russian culture.");

    }

    private void createInternal(String title, String author, String description){
        Long id = idCounter.getAndIncrement();
        store.put(id, new Book(id, title, author, description));
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD operations
    // ══════════════════════════════════════════════════════════

    public PagedResponse<Book> getAll(int page, int pageSize){
        List<Book> all = new ArrayList<>(store.values());
        all.sort(Comparator.comparing(Book::getBookAuthor));
        long total = all.size();
        int start  = page * pageSize;
        int end    = Math.min(start + pageSize, all.size());
        List<Book> pageContent = start >= all.size()
                ? Collections.emptyList()
                : all.subList(start, end);

        return new PagedResponse<>(pageContent, page, pageSize, total);

    }

    public Optional<Book> getById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Book create(BookRequest req) {
        Long id = idCounter.getAndIncrement();
        Book book = mapFromRequest(id, req);
        store.put(id, book);
        return book;
    }

    public Optional<Book> update(Long id, BookRequest req) {
        if (!store.containsKey(id)) return Optional.empty();
        Book book = mapFromRequest(id, req);
        store.put(id, book);
        return Optional.of(book);
    }
    public boolean delete(Long id) {
        return store.remove(id) != null;
    }

    public long count() {
        return store.size();
    }

    // ── Statistics ────────────────────────────────────────────
    public Map<String, Long> statsByAuthor() {
        return store.values().stream()
                .filter(book -> book.getBookAuthor() != null)
                .collect(Collectors.groupingBy(Book::getBookAuthor, Collectors.counting()));
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
    private void generateAndNotify() {
        Long id = idCounter.getAndIncrement();
        net.datafaker.providers.base.Book randomBook = faker.book();
        Book generated = new Book(id, randomBook.title(), randomBook.author(), randomBook.genre() );
        store.put(id, generated);
        messagingTemplate.convertAndSend("/topic/meetups", generated);
    }

    /** Oprește generatorul */
    public void stopGenerator() {
        if (generatorTask != null) {
            generatorTask.cancel(false);
            generatorTask = null;
        }
        generatorRunning = false;
    }

    // ── Mapper ────────────────────────────────────────────────
    private  Book mapFromRequest(long id, BookRequest request){
        return new Book(
                id, request.getBookTitle(), request.getBookAuthor(), request.getBookDescription()
        );
    }
}
