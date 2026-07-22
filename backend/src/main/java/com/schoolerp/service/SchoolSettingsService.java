package com.schoolerp.service;

import com.schoolerp.entity.SchoolSettings;
import com.schoolerp.repository.SchoolSettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class SchoolSettingsService {

    private final SchoolSettingsRepository repository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public SchoolSettingsService(SchoolSettingsRepository repository) {
        this.repository = repository;
    }

    public SchoolSettings getSettings() {
        return repository.findById(1L).orElseGet(() -> {
            SchoolSettings defaults = new SchoolSettings();
            defaults.setId(1L);
            defaults.setSchoolName("My School");
            return repository.save(defaults);
        });
    }

    public SchoolSettings updateSettings(SchoolSettings updated) {
        SchoolSettings existing = getSettings();
        existing.setSchoolName(updated.getSchoolName());
        existing.setTagline(updated.getTagline());
        existing.setAddress(updated.getAddress());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setWebsite(updated.getWebsite());
        existing.setEstablishedYear(updated.getEstablishedYear());
        existing.setPrincipalName(updated.getPrincipalName());
        return repository.save(existing);
    }

    public SchoolSettings uploadLogo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }

        SchoolSettings settings = getSettings();

        Path logosDir = Paths.get(uploadDir, "logos");
        Files.createDirectories(logosDir);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "logo";
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String filename = "logo-" + UUID.randomUUID() + extension;

        Path target = logosDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        settings.setLogoUrl("/uploads/logos/" + filename);
        return repository.save(settings);
    }
}
