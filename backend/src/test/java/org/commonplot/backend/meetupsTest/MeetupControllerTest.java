package org.commonplot.backend.meetupsTest;

import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.meetups.MeetupController;
import org.commonplot.backend.meetups.MeetupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonplot.backend.meetups.model.Meetup;
import org.commonplot.backend.PagedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ============================================================
//  MeetupControllerTest.java — teste HTTP layer
//  @WebMvcTest izolează controllerul, MeetupService e mock-uit
// ============================================================
@WebMvcTest(controllers = MeetupController.class)
class MeetupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private MeetupService meetupService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helpers ───────────────────────────────────────────────
    private Book sampleBook() {
        Book b = new Book("Atomic Habits", "James Clear", "Build habits.", null);
        b.setId(1);
        return b;
    }

    private Meetup sampleMeetup(long id) {
        Meetup m = new Meetup(
                "Test Club", "Cafe, Cluj", "2026-12-01T10:00",
                sampleBook(), 1, "testuser", 90, 4.5, "Test description"
        );
        m.setId(id);
        return m;
    }

    private Map<String, Object> validRequestBody() {
        Map<String, Object> map = new HashMap<>();
        map.put("titleEvent", "Test Book Club");
        map.put("location", "Test Cafe, Cluj");
        map.put("date", "2026-12-01T10:00");
        map.put("bookID", 1);
        map.put("ownerID", 1);
        map.put("ownerUsername", "testuser");
        map.put("duration", 90);
        map.put("rating", 4.5);
        map.put("description", "Test description");
        return map;
    }

    // ── GET /api/meetups ──────────────────────────────────────

    @Test
    void getAll_returns200WithPagedContent() throws Exception {
        PagedResponse<Meetup> mockPage = new PagedResponse<>(
                List.of(sampleMeetup(1L), sampleMeetup(2L)), 0, 10, 2
        );
        when(meetupService.getAll(0, 10)).thenReturn(mockPage);

        mockMvc.perform(get("/api/meetups?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void getAll_invalidPageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/meetups?page=-1&size=10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_invalidSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/meetups?page=0&size=0"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/meetups/{id} ─────────────────────────────────

    @Test
    void getById_returns200WhenFound() throws Exception {
        when(meetupService.getById(1L)).thenReturn(Optional.of(sampleMeetup(1L)));

        mockMvc.perform(get("/api/meetups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titleEvent").value("Test Club"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(meetupService.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/meetups/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/meetups ─────────────────────────────────────

    @Test
    void create_returns201WithValidBody() throws Exception {
        when(meetupService.create(any())).thenReturn(sampleMeetup(200L));

        mockMvc.perform(post("/api/meetups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(200));
    }

    @Test
    void create_returns400WhenTitleMissing() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.remove("titleEvent");

        mockMvc.perform(post("/api/meetups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenLocationMissing() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.remove("location");

        mockMvc.perform(post("/api/meetups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenDurationTooShort() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.put("duration", 5);

        mockMvc.perform(post("/api/meetups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenRatingTooHigh() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.put("rating", 6.0);

        mockMvc.perform(post("/api/meetups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenDateFormatInvalid() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.put("date", "not-a-date");

        mockMvc.perform(post("/api/meetups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/meetups/{id} ─────────────────────────────────

    @Test
    void update_returns200WhenFound() throws Exception {
        when(meetupService.update(eq(1L), any()))
                .thenReturn(Optional.of(sampleMeetup(1L)));

        mockMvc.perform(put("/api/meetups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody())))
                .andExpect(status().isOk());
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(meetupService.update(eq(99L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/meetups/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody())))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/meetups/{id} ──────────────────────────────

    @Test
    void delete_returns204WhenDeleted() throws Exception {
        when(meetupService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/meetups/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        when(meetupService.delete(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/meetups/99"))
                .andExpect(status().isNotFound());
    }

    // ── Statistics ────────────────────────────────────────────

    @Test
    void statsCount_returns200() throws Exception {
        when(meetupService.count()).thenReturn(42L);

        mockMvc.perform(get("/api/meetups/stats/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(42));
    }

    @Test
    void statsAverageRating_returns200() throws Exception {
        when(meetupService.averageRating()).thenReturn(4.3);

        mockMvc.perform(get("/api/meetups/stats/average-rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.3));
    }

    // ── Generator ─────────────────────────────────────────────

    @Test
    void startGenerator_returns200() throws Exception {
        mockMvc.perform(post("/api/meetups/generator/start?interval=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("started"));
    }

    @Test
    void startGenerator_invalidIntervalReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/meetups/generator/start?interval=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stopGenerator_returns200() throws Exception {
        mockMvc.perform(post("/api/meetups/generator/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("stopped"));
    }

    @Test
    void generatorStatus_returns200() throws Exception {
        when(meetupService.isGeneratorRunning()).thenReturn(false);

        mockMvc.perform(get("/api/meetups/generator/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false));
    }
}