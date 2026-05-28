package org.commonplot.backend.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthChallengeRepository extends JpaRepository<AuthChallenge, Integer> {
    Optional<AuthChallenge> findByUsernameAndUsedFalse(String username);
    Optional<AuthChallenge> findByNonceAndUsedFalse(String nonce);
}