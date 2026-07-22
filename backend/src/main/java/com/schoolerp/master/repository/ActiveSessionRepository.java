package com.schoolerp.master.repository;

import com.schoolerp.master.entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, Long> {
    Optional<ActiveSession> findBySchoolCodeIgnoreCaseAndUsername(String schoolCode, String username);
    void deleteBySchoolCodeIgnoreCaseAndUsername(String schoolCode, String username);
}
