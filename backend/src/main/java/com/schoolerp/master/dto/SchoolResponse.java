package com.schoolerp.master.dto;

import com.schoolerp.master.entity.School;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SchoolResponse {
    private Long id;
    private String schoolCode;
    private String schoolName;
    private boolean active;

    public static SchoolResponse from(School school) {
        return new SchoolResponse(school.getId(), school.getSchoolCode(), school.getSchoolName(), school.isActive());
    }
}
