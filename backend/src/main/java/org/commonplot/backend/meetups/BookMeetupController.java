package org.commonplot.backend.meetups;

import org.commonplot.backend.meetups.model.Meetup;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:5173")
public class BookMeetupController {

    private final MeetupService meetupService;

    public BookMeetupController(MeetupService meetupService) {
        this.meetupService = meetupService;
    }

    /**
     * GET /api/books/{bookId}/meetups
     * Returnează toate meetup-urile pentru un book specific
     */
    @GetMapping("/{bookId}/meetups")
    public ResponseEntity<List<Meetup>> getMeetupsByBook(@PathVariable Integer bookId) {
        List<Meetup> meetups = meetupService.getMeetupsByBookId(bookId);
        return ResponseEntity.ok(meetups);
    }

    /**
     * GET /api/books/{bookId}/meetups/count
     * Numărul de meetup-uri pentru un book
     */
    @GetMapping("/{bookId}/meetups/count")
    public ResponseEntity<Map<String, Long>> countMeetupsByBook(@PathVariable Integer bookId) {
        long count = meetupService.getMeetupsByBookId(bookId).size();
        return ResponseEntity.ok(Map.of("bookId", (long) bookId, "count", count));
    }

    /**
     * GET /api/books/{bookId}/meetups/stats
     * Statistici meetup-uri per book (rating mediu, durata medie)
     */
    @GetMapping("/{bookId}/meetups/stats")
    public ResponseEntity<Map<String, Object>> statsByBook(@PathVariable Integer bookId) {
        List<Meetup> meetups = meetupService.getMeetupsByBookId(bookId);

        double avgRating = meetups.stream()
                .filter(m -> m.getRating() != null)
                .mapToDouble(Meetup::getRating)
                .average()
                .orElse(0.0);

        double avgDuration = meetups.stream()
                .filter(m -> m.getDuration() != null)
                .mapToDouble(Meetup::getDuration)
                .average()
                .orElse(0.0);

        return ResponseEntity.ok(Map.of(
                "bookId",       bookId,
                "totalMeetups", meetups.size(),
                "avgRating",    Math.round(avgRating * 10.0) / 10.0,
                "avgDuration",  Math.round(avgDuration)
        ));
    }
}