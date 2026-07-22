package com.schoolerp.master.controller;

import com.schoolerp.master.dto.SchoolCreateRequest;
import com.schoolerp.master.dto.SchoolResponse;
import com.schoolerp.master.service.SchoolService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Only reachable by a SUPER_ADMIN-issued token (see SecurityConfig: /api/schools/** requires
 * ROLE_SUPER_ADMIN). Lets the platform owner register new schools and see what's registered.
 */
@RestController
@RequestMapping("/api/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public List<SchoolResponse> listSchools() {
        return schoolService.listSchools().stream().map(SchoolResponse::from).toList();
    }

    @PostMapping
    public SchoolResponse createSchool(@Valid @RequestBody SchoolCreateRequest request) {
        return SchoolResponse.from(schoolService.createSchool(request));
    }

    @PatchMapping("/{schoolCode}/active")
    public SchoolResponse setActive(@PathVariable String schoolCode, @RequestParam boolean active) {
        return SchoolResponse.from(schoolService.setActive(schoolCode, active));
    }
}
