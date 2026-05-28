package org.commonplot.backend.logging;

import org.commonplot.backend.logging.model.ActionLog;
import org.commonplot.backend.logging.model.SuspiciousUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// ============================================================
//  LoggingService.java — Gold
//  1. Persistă fiecare acțiune în action_logs
//  2. Detectează comportament malițios după 3 reguli:
//     - 3+ login-uri eșuate în 5 minute → brute force
//     - 10+ DELETE-uri în 1 minut → data destruction
//     - 50+ acțiuni în 1 minut → scraping/flooding
// ============================================================
@Service
public class LoggingService {

    private final ActionLogRepository      logRepository;
    private final SuspiciousUserRepository suspiciousRepository;

    // Praguri de detecție
    private static final int    FAILED_LOGIN_THRESHOLD  = 3;
    private static final int    FAILED_LOGIN_WINDOW_MIN = 5;
    private static final int    DELETE_THRESHOLD        = 10;
    private static final int    DELETE_WINDOW_MIN       = 1;
    private static final int    ACTION_THRESHOLD        = 50;
    private static final int    ACTION_WINDOW_MIN       = 1;

    public LoggingService(ActionLogRepository logRepository,
                          SuspiciousUserRepository suspiciousRepository) {
        this.logRepository      = logRepository;
        this.suspiciousRepository = suspiciousRepository;
    }

    // ── Logare acțiune ────────────────────────────────────────
    public ActionLog log(Integer userId, String username, String groupId,
                         String actionType, String actionInfo) {
        ActionLog entry = new ActionLog(userId, username, groupId, actionType, actionInfo);
        ActionLog saved = logRepository.save(entry);

        // Verifică comportament malițios după fiecare acțiune
        detectMaliciousBehaviour(userId, username);

        return saved;
    }

    // ── Detecție comportament malițios ─────────────────────────
    private void detectMaliciousBehaviour(Integer userId, String username) {
        if (username == null) return;

        // Regula 1: 3+ login-uri eșuate în 5 minute → brute force
        long failedLogins = logRepository.countFailedLogins(
                username, LocalDateTime.now().minusMinutes(FAILED_LOGIN_WINDOW_MIN)
        );
        if (failedLogins >= FAILED_LOGIN_THRESHOLD) {
            flagSuspicious(userId, username,
                    String.format("Brute force detected: %d failed login attempts in %d minutes",
                            failedLogins, FAILED_LOGIN_WINDOW_MIN));
        }

        // Regula 2: 10+ DELETE-uri în 1 minut → data destruction
        long deletes = logRepository.countDeletes(
                username, LocalDateTime.now().minusMinutes(DELETE_WINDOW_MIN)
        );
        if (deletes >= DELETE_THRESHOLD) {
            flagSuspicious(userId, username,
                    String.format("Data destruction detected: %d DELETE operations in %d minute(s)",
                            deletes, DELETE_WINDOW_MIN));
        }

        // Regula 3: 50+ acțiuni în 1 minut → flooding/scraping
        long totalActions = logRepository.countActions(
                username, LocalDateTime.now().minusMinutes(ACTION_WINDOW_MIN)
        );
        if (totalActions >= ACTION_THRESHOLD) {
            flagSuspicious(userId, username,
                    String.format("Flooding detected: %d actions in %d minute(s)",
                            totalActions, ACTION_WINDOW_MIN));
        }
    }

    // ── Marchează user ca suspicios (evită duplicate) ─────────
    private void flagSuspicious(Integer userId, String username, String reason) {
        // Nu adaugă duplicate — doar dacă nu e deja marcat ca suspicios nerezolvat
        if (!suspiciousRepository.existsByUsernameAndResolvedFalse(username)) {
            suspiciousRepository.save(new SuspiciousUser(userId, username, reason));
        }
    }

    // ── Queries pentru admin ───────────────────────────────────
    public List<ActionLog> getRecentLogs() {
        return logRepository.findTop100ByOrderByTimestampDesc();
    }

    public List<ActionLog> getLogsByUser(String username) {
        return logRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<SuspiciousUser> getActiveSuspicious() {
        return suspiciousRepository.findByResolvedFalseOrderByDetectedAtDesc();
    }

    public List<SuspiciousUser> getAllSuspicious() {
        return suspiciousRepository.findAllByOrderByDetectedAtDesc();
    }

    // ── Rezolvă un caz suspicios (admin action) ───────────────
    public boolean resolve(Integer id, String resolvedBy) {
        return suspiciousRepository.findById(id).map(s -> {
            s.setResolved(true);
            s.setResolvedAt(LocalDateTime.now());
            s.setResolvedBy(resolvedBy);
            suspiciousRepository.save(s);
            return true;
        }).orElse(false);
    }
}