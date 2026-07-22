package com.schoolerp.controller;

import com.schoolerp.entity.Attendance;
import com.schoolerp.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<Attendance> getByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam(required = false) Long classId) {
        if (classId != null) return attendanceService.findByClassAndDate(classId, date);
        return attendanceService.findByDate(date);
    }

    @GetMapping("/student/{studentId}")
    public List<Attendance> getByStudent(@PathVariable Long studentId) {
        return attendanceService.findByStudent(studentId);
    }

    @PostMapping
    public Attendance mark(@Valid @RequestBody Attendance attendance) {
        return attendanceService.markAttendance(attendance);
    }

    @PostMapping("/bulk")
    public List<Attendance> markBulk(@Valid @RequestBody List<Attendance> attendanceList) {
        return attendanceService.markBulkAttendance(attendanceList);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        attendanceService.delete(id);
    }
}
