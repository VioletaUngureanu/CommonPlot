package org.commonplot.backend.booksTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.commonplot.backend.books.BookController;
import org.commonplot.backend.books.BookService;
import org.commonplot.backend.books.model.Book;
import org.commonplot.backend.meetups.MeetupService;
import org.commonplot.backend.meetups.model.Meetup;
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
//  BookControllerTest.java — teste HTTP layer pentru BookController
//  BookService și MeetupService sunt mock-uite
// ============================================================
@WebMvcTest(controllers = BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private BookService bookService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private MeetupService meetupService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Helpers ───────────────────────────────────────────────
    private Book sampleBook(int id) {
        Book b = new Book("Atomic Habits", "James Clear",
                "Build good habits.", "https://example.com/cover.jpg");
        b.setId(id);
        return b;
    }

    private Map<String, Object> validRequestBody() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", "Atomic Habits");
        map.put("author", "James Clear");
        map.put("description", "Build good habits.");
        map.put("coverUrl", "https://example.com/cover.jpg");
        return map;
    }

    // ── GET /api/books ────────────────────────────────────────

    @Test
    void getAll_returns200WithBooks() throws Exception {
        when(bookService.getAll()).thenReturn(List.of(sampleBook(1), sampleBook(2)));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Atomic Habits"));
    }

    @Test
    void getAll_returns200WithEmptyList() throws Exception {
        when(bookService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/books/{id} ───────────────────────────────────

    @Test
    void getById_returns200WhenFound() throws Exception {
        when(bookService.getById(1)).thenReturn(Optional.of(sampleBook(1)));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Atomic Habits"))
                .andExpect(jsonPath("$.author").value("James Clear"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(bookService.getById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/books ───────────────────────────────────────

    @Test
    void create_returns201WithValidBody() throws Exception {
        when(bookService.create(any())).thenReturn(sampleBook(1));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Atomic Habits"));
    }

    @Test
    void create_returns400WhenTitleMissing() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.remove("title");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenAuthorMissing() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.remove("author");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenCoverUrlInvalid() throws Exception {
        Map<String, Object> body = new HashMap<>(validRequestBody());
        body.put("coverUrl", "not-a-url");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/books/{id} ───────────────────────────────────

    @Test
    void update_returns200WhenFound() throws Exception {
        when(bookService.update(eq(1), any())).thenReturn(Optional.of(sampleBook(1)));

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(bookService.update(eq(999), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody())))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/books/{id} ────────────────────────────────

    @Test
    void delete_returns204WhenDeleted() throws Exception {
        when(bookService.delete(1)).thenReturn(true);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404WhenNotFound() throws Exception {
        when(bookService.delete(999)).thenReturn(false);

        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/books/stats/count ────────────────────────────

    @Test
    void count_returns200() throws Exception {
        when(bookService.count()).thenReturn(5L);

        mockMvc.perform(get("/api/books/stats/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5));
    }

    // ── GET /api/books/{id}/meetups — relație 1-to-many ───────

    @Test
    void getMeetupsByBook_returns200WhenBookExists() throws Exception {
        when(bookService.getById(1)).thenReturn(Optional.of(sampleBook(1)));
        when(meetupService.getMeetupsByBookId(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/books/1/meetups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getMeetupsByBook_returns404WhenBookNotFound() throws Exception {
        when(bookService.getById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999/meetups"))
                .andExpect(status().isNotFound());
    }

    @Test
    void statsByBook_returns200WhenBookExists() throws Exception {
        when(bookService.getById(1)).thenReturn(Optional.of(sampleBook(1)));
        when(meetupService.getMeetupsByBookId(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/books/1/meetups/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.totalMeetups").value(0));
    }

    @Test
    void statsByBook_returns404WhenBookNotFound() throws Exception {
        when(bookService.getById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/999/meetups/stats"))
                .andExpect(status().isNotFound());
    }
}