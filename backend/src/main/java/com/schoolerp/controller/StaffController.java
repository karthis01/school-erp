package com.schoolerp.controller;

import com.schoolerp.entity.Staff;
import com.schoolerp.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<Staff> getAll() {
        return staffService.findAll();
    }

    @GetMapping("/{id}")
    public Staff getById(@PathVariable Long id) {
        return staffService.findById(id);
    }

    @PostMapping
    public Staff create(@Valid @RequestBody Staff staff) {
        return staffService.create(staff);
    }

    @PutMapping("/{id}")
    public Staff update(@PathVariable Long id, @Valid @RequestBody Staff staff) {
        return staffService.update(id, staff);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        staffService.delete(id);
    }
}
