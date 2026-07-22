package com.schoolerp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fee_structures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String feeType; // e.g. "Tuition Fee", "Transport Fee", "Library Fee"

    @ManyToOne
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass; // null = applies to all classes

    @Column(nullable = false)
    private Double amount;

    private String frequency; // e.g. "MONTHLY", "QUARTERLY", "ANNUAL", "ONE_TIME"

    private String academicYear;
}
