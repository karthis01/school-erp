package com.schoolerp.service;

import com.schoolerp.entity.Staff;
import com.schoolerp.exception.ResourceNotFoundException;
import com.schoolerp.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    public Staff findById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
    }

    public Staff create(Staff staff) {
        return staffRepository.save(staff);
    }

    public Staff update(Long id, Staff updated) {
        Staff existing = findById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDesignation(updated.getDesignation());
        existing.setDepartment(updated.getDepartment());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setDateOfJoining(updated.getDateOfJoining());
        existing.setSalary(updated.getSalary());
        existing.setStatus(updated.getStatus());
        return staffRepository.save(existing);
    }

    public void delete(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Staff not found with id: " + id);
        }
        staffRepository.deleteById(id);
    }
}
