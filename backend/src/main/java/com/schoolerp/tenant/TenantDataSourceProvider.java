package com.schoolerp.tenant;

import com.schoolerp.master.entity.School;
import com.schoolerp.master.repository.SchoolRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lazily builds one small HikariCP connection pool per school, keyed by school code, and
 * caches it for reuse. This is what lets new schools be registered and used immediately
 * without restarting the application.
 */
@Component
public class TenantDataSourceProvider {

    private final SchoolRepository schoolRepository;
    private final Map<String, DataSource> pools = new ConcurrentHashMap<>();

    public TenantDataSourceProvider(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    /**
     * Returns the (cached) DataSource for the given school code, looking it up in the
     * master "schools" table and building a new pool on first use.
     */
    public DataSource getDataSource(String schoolCode) {
        if (schoolCode == null || schoolCode.isBlank()) {
            throw new TenantNotFoundException("No school code was supplied on this request");
        }
        String key = schoolCode.toLowerCase();
        DataSource existing = pools.get(key);
        if (existing != null) {
            return existing;
        }
        School school = schoolRepository.findBySchoolCodeIgnoreCaseAndActiveTrue(key)
                .orElseThrow(() -> new TenantNotFoundException("Unknown or inactive school code: " + schoolCode));
        return pools.computeIfAbsent(key, k -> buildDataSource(school));
    }

    /** Used right after a School row is created, so we don't have to look it up again. */
    public DataSource getOrCreate(School school) {
        return pools.computeIfAbsent(school.getSchoolCode().toLowerCase(), k -> buildDataSource(school));
    }

    /** Call after editing a school's DB credentials, so the old pool is dropped and rebuilt. */
    public void evict(String schoolCode) {
        DataSource removed = pools.remove(schoolCode.toLowerCase());
        if (removed instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }

    private DataSource buildDataSource(School school) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(school.getDbUrl());
        config.setUsername(school.getDbUsername());
        config.setPassword(school.getDbPassword());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("tenant-" + school.getSchoolCode());
        // Kept small per-tenant; tune based on how many schools you expect to run concurrently.
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        return new HikariDataSource(config);
    }
}
