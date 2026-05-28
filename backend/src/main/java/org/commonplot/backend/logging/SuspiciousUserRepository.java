package org.commonplot.backend.logging;

import org.commonplot.backend.logging.model.SuspiciousUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspiciousUserRepository extends JpaRepository<SuspiciousUser, Integer> {

    List<SuspiciousUser> findByResolvedFalseOrderByDetectedAtDesc();

    List<SuspiciousUser> findAllByOrderByDetectedAtDesc();

    boolean existsByUsernameAndResolvedFalse(String username);
}