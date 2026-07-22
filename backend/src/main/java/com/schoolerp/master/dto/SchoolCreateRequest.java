package com.schoolerp.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SchoolCreateRequest {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_-]{2,40}$", message = "School code may only contain letters, numbers, - and _")
    private String schoolCode; // e.g. "svm"

    @NotBlank
    private String schoolName;

    @NotBlank
    private String dbHost = "localhost";

    private Integer dbPort = 3306;

    // If left blank, defaults to "school_<schoolCode>"
    private String dbName;

    @NotBlank
    private String dbUsername;

    @NotBlank
    private String dbPassword;

    // Set false only when pointing at a database that already has the school-erp tables
    // (e.g. migrating an existing single-school install into this multi-school setup).
    private boolean initializeSchema = true;

    // Default login for the new school will be "admin@<schoolCode>" / this password.
    private String defaultAdminPassword = "admin123";

    // If true (and initializeSchema is true), populates 5 demo records per module so the
    // new school isn't completely empty on first login.
    private boolean seedSampleData = false;
}
