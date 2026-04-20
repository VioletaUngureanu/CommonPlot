package org.commonplot.backend.meetups;

import org.commonplot.backend.meetups.model.MeetupRequest;
import org.commonplot.backend.PagedResponse;
import org.commonplot.backend.meetups.model.Meetup;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ============================================================
//  MeetupGraphQLController.java — Gold challenge
//  Reimplementare prin GraphQL a aceluiași service layer
//  Același MeetupService, altă interfață
// ============================================================
@Controller
public class MeetupGraphQLController {

    private final MeetupService meetupService;

    public MeetupGraphQLController(MeetupService meetupService) {
        this.meetupService = meetupService;
    }

    // ── Queries ───────────────────────────────────────────────

    @QueryMapping
    public PagedResponse<Meetup> meetups(
            @Argument Integer page,
            @Argument Integer size
    ) {
        return meetupService.getAll(
                page != null ? page : 0,
                size != null ? size : 10
        );
    }

    @QueryMapping
    public Meetup meetup(@Argument Long id) {
        return meetupService.getById(id).orElse(null);
    }

    @QueryMapping
    public Map<String, Object> stats() {
        return Map.of(
                "total",         meetupService.count(),
                "averageRating", meetupService.averageRating()
        );
    }

    @QueryMapping
    public List<Map<String, Object>> statsByLocation() {
        return meetupService.statsByLocation().entrySet().stream()
                .map(e -> Map.<String, Object>of("location", e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public List<Map<String, Object>> statsByBook() {
        return meetupService.statsByBook().entrySet().stream()
                .map(e -> Map.<String, Object>of("book", e.getKey(), "count", e.getValue()))
                .collect(Collectors.toList());
    }

    // ── Mutations ─────────────────────────────────────────────

    @MutationMapping
    public Meetup createMeetup(@Argument Map<String, Object> input) {
        return meetupService.create(mapToRequest(input));
    }

    @MutationMapping
    public Meetup updateMeetup(@Argument Long id, @Argument Map<String, Object> input) {
        return meetupService.update(id, mapToRequest(input)).orElse(null);
    }

    @MutationMapping
    public boolean deleteMeetup(@Argument Long id) {
        return meetupService.delete(id);
    }

    @MutationMapping
    public boolean startGenerator(@Argument Integer interval) {
        meetupService.startGenerator(interval != null ? interval : 2);
        return true;
    }

    @MutationMapping
    public boolean stopGenerator() {
        meetupService.stopGenerator();
        return true;
    }

    // ── Mapper ────────────────────────────────────────────────
    private MeetupRequest mapToRequest(Map<String, Object> input) {
        MeetupRequest req = new MeetupRequest();
        req.setTitleEvent((String) input.get("titleEvent"));
        req.setLocation((String) input.get("location"));
        req.setDate((String) input.get("date"));
        req.setBookId((Integer) input.get("bookId"));
        req.setBookTitle((String) input.get("bookTitle"));
        req.setBookAuthor((String) input.get("bookAuthor"));
        req.setOwnerID((Integer) input.get("ownerID"));
        req.setOwnerUsername((String) input.get("ownerUsername"));
        req.setDuration((Integer) input.get("duration"));
        Object rating = input.get("rating");
        if (rating instanceof Number) req.setRating(((Number) rating).doubleValue());
        req.setDescription((String) input.get("description"));
        return req;
    }
}