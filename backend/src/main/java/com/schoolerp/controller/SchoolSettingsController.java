package com.schoolerp.controller;

import com.schoolerp.entity.SchoolSettings;
import com.schoolerp.service.SchoolSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/settings")
public class SchoolSettingsController {

    private final SchoolSettingsService service;

    public SchoolSettingsController(SchoolSettingsService service) {
        this.service = service;
    }

    @GetMapping
    public SchoolSettings getSettings() {
        return service.getSettings();
    }

    @PutMapping
    public SchoolSettings updateSettings(@Valid @RequestBody SchoolSettings settings) {
        return service.updateSettings(settings);
    }

    @PostMapping("/logo")
    public SchoolSettings uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        return service.uploadLogo(file);
    }
}
