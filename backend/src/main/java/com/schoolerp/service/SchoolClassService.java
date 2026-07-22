package com.schoolerp.service;

import com.schoolerp.entity.SchoolClass;
import com.schoolerp.entity.Staff;
import com.schoolerp.exception.ResourceNotFoundException;
import com.schoolerp.repository.SchoolClassRepository;
import com.schoolerp.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolClassService {

    private final SchoolClassRepository schoolClassRepository;
    private final StaffRepository staffRepository;

    public SchoolClassService(SchoolClassRepository schoolClassRepository, StaffRepository staffRepository) {
        this.schoolClassRepository = schoolClassRepository;
        this.staffRepository = staffRepository;
    }

    public List<SchoolClass> findAll() {
        return schoolClassRepository.findAll();
    }

    public SchoolClass findById(Long id) {
        return schoolClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));
    }

    public SchoolClass create(SchoolClass schoolClass) {
        attachTeacher(schoolClass);
        return schoolClassRepository.save(schoolClass);
    }

    public SchoolClass update(Long id, SchoolClass updated) {
        SchoolClass existing = findById(id);
        existing.setClassName(updated.getClassName());
        existing.setSection(updated.getSection());
        existing.setAcademicYear(updated.getAcademicYear());
        attachTeacher(updated);
        existing.setClassTeacher(updated.getClassTeacher());
        return schoolClassRepository.save(existing);
    }

    public void delete(Long id) {
        if (!schoolClassRepository.existsById(id)) {
            throw new ResourceNotFoundException("Class not found with id: " + id);
        }
        schoolClassRepository.deleteById(id);
    }

    private void attachTeacher(SchoolClass schoolClass) {
        if (schoolClass.getClassTeacher() != null && schoolClass.getClassTeacher().getId() != null) {
            Staff teacher = staffRepository.findById(schoolClass.getClassTeacher().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
            schoolClass.setClassTeacher(teacher);
        }
    }
}
