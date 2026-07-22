package com.schoolerp.controller;

import com.schoolerp.entity.SchoolClass;
import com.schoolerp.service.SchoolClassService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class SchoolClassController {

    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @GetMapping
    public List<SchoolClass> getAll() {
        return schoolClassService.findAll();
    }

    @GetMapping("/{id}")
    public SchoolClass getById(@PathVariable Long id) {
        return schoolClassService.findById(id);
    }

    @PostMapping
    public SchoolClass create(@Valid @RequestBody SchoolClass schoolClass) {
        return schoolClassService.create(schoolClass);
    }

    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable Long id, @Valid @RequestBody SchoolClass schoolClass) {
        return schoolClassService.update(id, schoolClass);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        schoolClassService.delete(id);
    }
}
