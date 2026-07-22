package com.schoolerp.config;

import com.schoolerp.master.entity.SuperAdmin;
import com.schoolerp.master.repository.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the platform-level super-admin account on first run, in the MASTER database.
 * This account has no "@school" suffix and logs in to manage schools (see AuthController,
 * SchoolController). Per-school "admin" users are instead seeded automatically whenever a
 * new school is registered - see SchoolService.
 */
@Configuration
public class DataSeeder {

    @Value("${app.seed.superadmin.username}")
    private String superAdminUsername;

    @Value("${app.seed.superadmin.password}")
    private String superAdminPassword;

    @Bean
    public CommandLineRunner seedSuperAdmin(SuperAdminRepository superAdminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!superAdminRepository.existsByUsername(superAdminUsername)) {
                SuperAdmin superAdmin = new SuperAdmin();
                superAdmin.setUsername(superAdminUsername);
                superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
                superAdmin.setEnabled(true);
                superAdminRepository.save(superAdmin);
                System.out.println("Seeded platform super-admin -> username: " + superAdminUsername
                        + " / password: " + superAdminPassword
                        + "  (log in with this plain username - no @school - to register schools)");
            }
        };
    }
}
