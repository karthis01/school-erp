package com.schoolerp.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A platform-level account (not tied to any single school) that can register and manage
 * schools. Logs in with a plain username that does NOT contain "@" - e.g. "sysadmin" -
 * as opposed to school users who log in as "username@schoolcode".
 */
@Entity
@Table(name = "super_admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;
}
