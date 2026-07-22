package com.schoolerp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "school_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolSettings {

    @Id
    private Long id; // always 1 - this table only ever holds a single row

    private String schoolName;
    private String tagline;
    private String address;
    private String phone;
    private String email;
    private String website;
    private Integer establishedYear;
    private String principalName;
    private String logoUrl;
}
