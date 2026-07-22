package com.schoolerp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String className; // e.g. "Grade 5"

    @Column(nullable = false)
    private String section; // e.g. "A"

    private String academicYear; // e.g. "2026-2027"

    @ManyToOne
    @JoinColumn(name = "class_teacher_id")
    private Staff classTeacher;

    @OneToMany(mappedBy = "schoolClass", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Student> students = new ArrayList<>();
}
