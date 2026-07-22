package com.schoolerp.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Registry row for one school (tenant). Lives in the MASTER database only.
 * schoolCode is the short code used in logins, e.g. "svm" for admin@svm.
 */
@Entity
@Table(name = "schools")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Short unique code, lowercase, used in "username@schoolCode" logins. e.g. "svm"
    @Column(name = "school_code", nullable = false, unique = true, length = 40)
    private String schoolCode;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    // Full JDBC URL for this school's own database, e.g.
    // jdbc:mysql://localhost:3306/school_svm?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    @Column(name = "db_url", nullable = false, length = 500)
    private String dbUrl;

    @Column(name = "db_username", nullable = false)
    private String dbUsername;

    // NOTE: for production, encrypt this column or move credentials to a secrets manager.
    @Column(name = "db_password", nullable = false)
    private String dbPassword;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
