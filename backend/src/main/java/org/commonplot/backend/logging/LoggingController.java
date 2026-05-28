package org.commonplot.backend.logging;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.commonplot.backend.logging.model.*;

import java.util.List;
import java.util.Map;

// ============================================================
//  LoggingController.java — Gold
//  Endpoints pentru admin:
//  GET  /api/admin/logs              — ultimele 100 log-uri
//  GET  /api/admin/logs/user/{u}     — log-urile unui user
//  GET  /api/admin/suspicious        — useri suspicioși activi
//  GET  /api/admin/suspicious/all    — toți (inclusiv rezolvați)
//  POST /api/admin/suspicious/{id}/resolve — marchează ca rezolvat
// ============================================================
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*")
public class LoggingController {

    private final LoggingService loggingService;

    public LoggingController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ActionLog>> getLogs() {
        return ResponseEntity.ok(loggingService.getRecentLogs());
    }

    @GetMapping("/logs/user/{username}")
    public ResponseEntity<List<ActionLog>> getLogsByUser(@PathVariable String username) {
        return ResponseEntity.ok(loggingService.getLogsByUser(username));
    }

    @GetMapping("/suspicious")
    public ResponseEntity<List<SuspiciousUser>> getActiveSuspicious() {
        return ResponseEntity.ok(loggingService.getActiveSuspicious());
    }

    @GetMapping("/suspicious/all")
    public ResponseEntity<List<SuspiciousUser>> getAllSuspicious() {
        return ResponseEntity.ok(loggingService.getAllSuspicious());
    }

    @PostMapping("/suspicious/{id}/resolve")
    public ResponseEntity<?> resolve(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body
    ) {
        String resolvedBy = body.getOrDefault("resolvedBy", "admin");
        boolean ok = loggingService.resolve(id, resolvedBy);
        return ok
                ? ResponseEntity.ok(Map.of("status", "resolved"))
                : ResponseEntity.notFound().build();
    }
}