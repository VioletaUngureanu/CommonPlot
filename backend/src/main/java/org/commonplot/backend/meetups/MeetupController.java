package org.commonplot.backend.meetups;

import org.commonplot.backend.logging.LoggingService;
import org.commonplot.backend.meetups.model.MeetupRequest;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/meetups")
@CrossOrigin(origins = "*")
public class MeetupController {

    private final MeetupService  meetupService;
    private final LoggingService loggingService;

    public MeetupController(MeetupService meetupService, LoggingService loggingService) {
        this.meetupService  = meetupService;
        this.loggingService = loggingService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<Meetup>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        if (page < 0) return ResponseEntity.badRequest().build();
        if (size < 1 || size > 100) return ResponseEntity.badRequest().build();
        if (username != null)
            loggingService.log(null, username, role != null ? role : "USER",
                    "READ_MEETUPS", "Page " + page + " size " + size);
        return ResponseEntity.ok(meetupService.getAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meetup> getById(@PathVariable Long id) {
        return meetupService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Meetup> create(
            @Valid @RequestBody MeetupRequest request,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        Meetup created = meetupService.create(request);
        loggingService.log(null, username != null ? username : "unknown",
                role != null ? role : "USER",
                "CREATE_MEETUP", "Created meetup ID " + created.getId() + ": " + created.getTitleEvent());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meetup> update(
            @PathVariable Long id,
            @Valid @RequestBody MeetupRequest request,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        return meetupService.update(id, request)
                .map(updated -> {
                    loggingService.log(null, username != null ? username : "unknown",
                            role != null ? role : "USER",
                            "UPDATE_MEETUP", "Updated meetup ID " + id);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-Role",     required = false) String role
    ) {
        if (meetupService.delete(id)) {
            loggingService.log(null, username != null ? username : "unknown",
                    role != null ? role : "USER",
                    "DELETE_MEETUP", "Deleted meetup ID " + id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("total", meetupService.count()));
    }

    @GetMapping("/stats/by-location")
    public ResponseEntity<Map<String, Long>> statsByLocation() {
        return ResponseEntity.ok(meetupService.statsByLocation());
    }

    @GetMapping("/stats/by-book")
    public ResponseEntity<Map<String, Long>> statsByBook() {
        return ResponseEntity.ok(meetupService.statsByBook());
    }

    @GetMapping("/stats/average-rating")
    public ResponseEntity<Map<String, Double>> averageRating() {
        return ResponseEntity.ok(Map.of("averageRating", meetupService.averageRating()));
    }

    @PostMapping("/generator/start")
    public ResponseEntity<Map<String, String>> startGenerator(
            @RequestParam(defaultValue = "2") int interval
    ) {
        if (interval < 1 || interval > 60)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Interval must be between 1 and 60 seconds."));
        meetupService.startGenerator(interval);
        return ResponseEntity.ok(Map.of("status", "started", "interval", interval + "s"));
    }

    @PostMapping("/generator/stop")
    public ResponseEntity<Map<String, String>> stopGenerator() {
        meetupService.stopGenerator();
        return ResponseEntity.ok(Map.of("status", "stopped"));
    }

    @GetMapping("/generator/status")
    public ResponseEntity<Map<String, Boolean>> generatorStatus() {
        return ResponseEntity.ok(Map.of("running", meetupService.isGeneratorRunning()));
    }
}