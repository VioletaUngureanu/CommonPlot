package org.commonplot.backend.meetups;

import org.commonplot.backend.meetups.model.MeetupRequest;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

// ============================================================
//  MeetupController.java — endpoint-uri REST
//  Separate complet de logica de business (MeetupService)
//  Validarea e delegată la DTO (@Valid + Jakarta annotations)
// ============================================================
@RestController
@RequestMapping("/api/meetups")
@CrossOrigin(origins = "*")  // Vue frontend
public class MeetupController {

    private final MeetupService meetupService;

    public MeetupController(MeetupService meetupService) {
        this.meetupService = meetupService;
    }

    // ══════════════════════════════════════════════════════════
    //  CRUD Endpoints
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/meetups?page=0&size=10
     * Returnează o pagină de meetup-uri (paginare server-side)
     */
    @GetMapping
    public ResponseEntity<PagedResponse<Meetup>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 0)  return ResponseEntity.badRequest().build();
        if (size < 1 || size > 100) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(meetupService.getAll(page, size));
    }

    /**
     * GET /api/meetups/{id}
     * Returnează un meetup după ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Meetup> getById(@PathVariable Long id) {
        return meetupService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/meetups
     * Creează un meetup nou — @Valid declanșează validarea Jakarta
     */
    @PostMapping
    public ResponseEntity<Meetup> create(@Valid @RequestBody MeetupRequest request) {
        Meetup created = meetupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/meetups/{id}
     * Actualizează un meetup existent
     */
    @PutMapping("/{id}")
    public ResponseEntity<Meetup> update(
            @PathVariable Long id,
            @Valid @RequestBody MeetupRequest request
    ) {
        return meetupService.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/meetups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (meetupService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ══════════════════════════════════════════════════════════
    //  Statistics Endpoints
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/meetups/stats/count
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("total", meetupService.count()));
    }

    /**
     * GET /api/meetups/stats/by-location
     */
    @GetMapping("/stats/by-location")
    public ResponseEntity<Map<String, Long>> statsByLocation() {
        return ResponseEntity.ok(meetupService.statsByLocation());
    }

    /**
     * GET /api/meetups/stats/by-book
     */
    @GetMapping("/stats/by-book")
    public ResponseEntity<Map<String, Long>> statsByBook() {
        return ResponseEntity.ok(meetupService.statsByBook());
    }

    /**
     * GET /api/meetups/stats/average-rating
     */
    @GetMapping("/stats/average-rating")
    public ResponseEntity<Map<String, Double>> averageRating() {
        return ResponseEntity.ok(Map.of("averageRating", meetupService.averageRating()));
    }

    /**
     * POST /api/meetups/generator/start?interval=2
     * Pornește generatorul automat (interval în secunde)
     */
    @PostMapping("/generator/start")
    public ResponseEntity<Map<String, String>> startGenerator(
            @RequestParam(defaultValue = "2") int interval
    ) {
        if (interval < 1 || interval > 60) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Interval must be between 1 and 60 seconds."));
        }
        meetupService.startGenerator(interval);
        return ResponseEntity.ok(Map.of(
                "status", "started",
                "interval", interval + "s"
        ));
    }

    /**
     * POST /api/meetups/generator/stop
     * Oprește generatorul automat
     */
    @PostMapping("/generator/stop")
    public ResponseEntity<Map<String, String>> stopGenerator() {
        meetupService.stopGenerator();
        return ResponseEntity.ok(Map.of("status", "stopped"));
    }

    /**
     * GET /api/meetups/generator/status
     */
    @GetMapping("/generator/status")
    public ResponseEntity<Map<String, Boolean>> generatorStatus() {
        return ResponseEntity.ok(Map.of("running", meetupService.isGeneratorRunning()));
    }
}