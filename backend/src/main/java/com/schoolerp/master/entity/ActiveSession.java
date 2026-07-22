package com.schoolerp.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row per currently-logged-in (schoolCode, username) pair, in the MASTER database.
 * tokenId is the JWT's "jti" claim for whichever token is currently allowed to be used.
 * When a user force-logs-in from a new place, this row is overwritten with the new jti,
 * which silently invalidates the old token (JwtAuthFilter checks jti still matches).
 */
@Entity
@Table(name = "active_sessions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_active_session_school_user", columnNames = {"school_code", "username"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_code", nullable = false, length = 40)
    private String schoolCode;

    @Column(nullable = false)
    private String username;

    @Column(name = "token_id", nullable = false, length = 64)
    private String tokenId;

    @Column(name = "login_time", nullable = false)
    private Instant loginTime;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "ip_address")
    private String ipAddress;
}
