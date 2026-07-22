package com.schoolerp.repository;

import com.schoolerp.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmployeeCode(String employeeCode);
}
