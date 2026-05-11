package org.commonplot.backend.logging;

import org.commonplot.backend.logging.model.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> {

    List<ActionLog> findTop100ByOrderByTimestampDesc();

    List<ActionLog> findByUsernameOrderByTimestampDesc(String username);

    // Câte login-uri eșuate a avut un user în ultimele N minute
    @Query("SELECT COUNT(l) FROM ActionLog l WHERE l.username = :username " +
            "AND l.actionType = 'LOGIN_FAILED' AND l.timestamp > :since")
    long countFailedLogins(String username, LocalDateTime since);

    // Câte DELETE-uri a făcut un user în ultimul minut
    @Query("SELECT COUNT(l) FROM ActionLog l WHERE l.username = :username " +
            "AND l.actionType LIKE 'DELETE%' AND l.timestamp > :since")
    long countDeletes(String username, LocalDateTime since);

    // Câte acțiuni totale a făcut un user în ultimul minut
    @Query("SELECT COUNT(l) FROM ActionLog l WHERE l.username = :username " +
            "AND l.timestamp > :since")
    long countActions(String username, LocalDateTime since);
}