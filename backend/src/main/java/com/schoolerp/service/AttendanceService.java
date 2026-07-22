package com.schoolerp.service;

import com.schoolerp.entity.Attendance;
import com.schoolerp.entity.Student;
import com.schoolerp.exception.ResourceNotFoundException;
import com.schoolerp.repository.AttendanceRepository;
import com.schoolerp.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    public List<Attendance> findByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }

    public List<Attendance> findByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public List<Attendance> findByClassAndDate(Long classId, LocalDate date) {
        return attendanceRepository.findByStudent_SchoolClass_IdAndDate(classId, date);
    }

    public Attendance markAttendance(Attendance attendance) {
        Student student = studentRepository.findById(attendance.getStudent().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        attendance.setStudent(student);

        // Upsert: one attendance record per student per day
        attendanceRepository.findByStudentIdAndDate(student.getId(), attendance.getDate())
                .ifPresent(existing -> attendance.setId(existing.getId()));

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> markBulkAttendance(List<Attendance> records) {
        return records.stream().map(this::markAttendance).toList();
    }

    public void delete(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Attendance record not found with id: " + id);
        }
        attendanceRepository.deleteById(id);
    }
}
