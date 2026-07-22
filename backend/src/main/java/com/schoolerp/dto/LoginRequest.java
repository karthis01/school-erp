package com.schoolerp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    // Tenant users log in as "username@schoolcode", e.g. "admin@svm".
    // Super-admin (platform) accounts log in with a plain username, no "@".
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // If true, forcibly logs out any existing active session for this user and continues.
    // Set after the user has confirmed the "already logged in elsewhere" prompt.
    private boolean force = false;
}
