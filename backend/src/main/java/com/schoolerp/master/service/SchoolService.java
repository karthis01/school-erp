package com.schoolerp.master.service;

import com.schoolerp.config.SampleDataSeeder;
import com.schoolerp.master.dto.SchoolCreateRequest;
import com.schoolerp.master.entity.School;
import com.schoolerp.master.repository.SchoolRepository;
import com.schoolerp.tenant.TenantContext;
import com.schoolerp.tenant.TenantDataSourceProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Service
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final TenantDataSourceProvider dataSourceProvider;
    private final TenantSchemaInitializer schemaInitializer;
    private final PasswordEncoder passwordEncoder;
    private final SampleDataSeeder sampleDataSeeder;

    public SchoolService(SchoolRepository schoolRepository,
                          TenantDataSourceProvider dataSourceProvider,
                          TenantSchemaInitializer schemaInitializer,
                          PasswordEncoder passwordEncoder,
                          SampleDataSeeder sampleDataSeeder) {
        this.schoolRepository = schoolRepository;
        this.dataSourceProvider = dataSourceProvider;
        this.schemaInitializer = schemaInitializer;
        this.passwordEncoder = passwordEncoder;
        this.sampleDataSeeder = sampleDataSeeder;
    }

    public List<School> listSchools() {
        return schoolRepository.findAllByOrderBySchoolNameAsc();
    }

    @Transactional("masterTransactionManager")
    public School createSchool(SchoolCreateRequest request) {
        String code = request.getSchoolCode().trim().toLowerCase();
        if (schoolRepository.existsBySchoolCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("A school with code '" + code + "' already exists");
        }

        String dbName = (request.getDbName() == null || request.getDbName().isBlank())
                ? "school_" + code
                : request.getDbName().trim();

        String jdbcUrl = "jdbc:mysql://" + request.getDbHost() + ":" + request.getDbPort() + "/" + dbName
                + "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        School school = new School();
        school.setSchoolCode(code);
        school.setSchoolName(request.getSchoolName().trim());
        school.setDbUrl(jdbcUrl);
        school.setDbUsername(request.getDbUsername());
        school.setDbPassword(request.getDbPassword());
        school.setActive(true);
        school = schoolRepository.save(school);

        DataSource tenantDataSource = dataSourceProvider.getOrCreate(school);

        if (request.isInitializeSchema()) {
            schemaInitializer.createSchema(tenantDataSource);
            seedDefaultAdmin(tenantDataSource, request.getDefaultAdminPassword());

            if (request.isSeedSampleData()) {
                try {
                    TenantContext.setCurrentSchool(code);
                    sampleDataSeeder.seedSampleData();
                } finally {
                    TenantContext.clear();
                }
            }
        }

        return school;
    }

    @Transactional("masterTransactionManager")
    public School setActive(String schoolCode, boolean active) {
        School school = schoolRepository.findBySchoolCodeIgnoreCase(schoolCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown school code: " + schoolCode));
        school.setActive(active);
        School saved = schoolRepository.save(school);
        if (!active) {
            dataSourceProvider.evict(schoolCode);
        }
        return saved;
    }

    private void seedDefaultAdmin(DataSource tenantDataSource, String rawPassword) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tenantDataSource);
        String password = (rawPassword == null || rawPassword.isBlank()) ? "admin123" : rawPassword;
        jdbcTemplate.update(
                "INSERT INTO users (username, password, full_name, email, role, enabled) VALUES (?, ?, ?, ?, ?, ?)",
                "admin", passwordEncoder.encode(password), "School Administrator", null, "ADMIN", true
        );
    }
}
