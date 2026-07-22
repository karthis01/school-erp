package com.schoolerp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String fullName;
    private String role;
    private String schoolCode; // null for super-admin logins
    private String schoolName; // null for super-admin logins
}
