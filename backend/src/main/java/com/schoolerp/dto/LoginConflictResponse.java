package com.schoolerp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Returned with HTTP 409 when the account already has a live session elsewhere. */
@Data
@AllArgsConstructor
public class LoginConflictResponse {
    private boolean conflict = true;
    private String message;
}
