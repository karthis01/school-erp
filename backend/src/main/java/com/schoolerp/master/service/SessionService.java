package com.schoolerp.master.service;

import com.schoolerp.master.entity.ActiveSession;
import com.schoolerp.master.repository.ActiveSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SessionService {

    private final ActiveSessionRepository activeSessionRepository;

    public SessionService(ActiveSessionRepository activeSessionRepository) {
        this.activeSessionRepository = activeSessionRepository;
    }

    /** True if this (school, username) currently has a live session recorded. */
    @Transactional(value = "masterTransactionManager", readOnly = true)
    public boolean hasActiveSession(String schoolCode, String username) {
        return activeSessionRepository.findBySchoolCodeIgnoreCaseAndUsername(schoolCode, username).isPresent();
    }

    /**
     * Starts (or overwrites) the session for this user with a new token id. Overwriting is
     * exactly what enforces "single session": the previous jti stops matching, so the old
     * token is rejected by JwtAuthFilter on its next use.
     */
    @Transactional("masterTransactionManager")
    public void startSession(String schoolCode, String username, String tokenId, String ipAddress) {
        ActiveSession session = activeSessionRepository
                .findBySchoolCodeIgnoreCaseAndUsername(schoolCode, username)
                .orElseGet(ActiveSession::new);
        session.setSchoolCode(schoolCode.toLowerCase());
        session.setUsername(username);
        session.setTokenId(tokenId);
        session.setLoginTime(Instant.now());
        session.setLastSeen(Instant.now());
        session.setIpAddress(ipAddress);
        activeSessionRepository.save(session);
    }

    /** Called by JwtAuthFilter on every request: is this exact token still the active one? */
    @Transactional("masterTransactionManager")
    public boolean isTokenStillActive(String schoolCode, String username, String tokenId) {
        return activeSessionRepository.findBySchoolCodeIgnoreCaseAndUsername(schoolCode, username)
                .map(session -> {
                    boolean matches = session.getTokenId().equals(tokenId);
                    if (matches) {
                        session.setLastSeen(Instant.now());
                        activeSessionRepository.save(session);
                    }
                    return matches;
                })
                .orElse(false);
    }

    @Transactional("masterTransactionManager")
    public void endSession(String schoolCode, String username) {
        activeSessionRepository.deleteBySchoolCodeIgnoreCaseAndUsername(schoolCode, username);
    }
}
