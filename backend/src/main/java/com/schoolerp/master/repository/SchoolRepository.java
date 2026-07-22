package com.schoolerp.master.repository;

import com.schoolerp.master.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySchoolCodeIgnoreCase(String schoolCode);
    Optional<School> findBySchoolCodeIgnoreCaseAndActiveTrue(String schoolCode);
    boolean existsBySchoolCodeIgnoreCase(String schoolCode);
    List<School> findAllByOrderBySchoolNameAsc();
}
