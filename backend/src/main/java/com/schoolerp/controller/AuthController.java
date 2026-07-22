package com.schoolerp.controller;

import com.schoolerp.dto.LoginConflictResponse;
import com.schoolerp.dto.LoginRequest;
import com.schoolerp.dto.LoginResponse;
import com.schoolerp.dto.RegisterUserRequest;
import com.schoolerp.entity.Role;
import com.schoolerp.entity.User;
import com.schoolerp.master.entity.School;
import com.schoolerp.master.entity.SuperAdmin;
import com.schoolerp.master.repository.SchoolRepository;
import com.schoolerp.master.repository.SuperAdminRepository;
import com.schoolerp.master.service.SessionService;
import com.schoolerp.repository.UserRepository;
import com.schoolerp.security.CustomUserDetails;
import com.schoolerp.security.JwtUtil;
import com.schoolerp.tenant.TenantContext;
import com.schoolerp.tenant.TenantNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolRepository schoolRepository;
    private final SuperAdminRepository superAdminRepository;
    private final SessionService sessionService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                           UserRepository userRepository, PasswordEncoder passwordEncoder,
                           SchoolRepository schoolRepository, SuperAdminRepository superAdminRepository,
                           SessionService sessionService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.schoolRepository = schoolRepository;
        this.superAdminRepository = superAdminRepository;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String raw = request.getUsername().trim();

        if (raw.contains("@")) {
            return tenantLogin(raw, request, httpRequest);
        }
        return superAdminLogin(raw, request.getPassword());
    }

    /** username@schoolcode login, scoped to that school's own database. */
    private ResponseEntity<?> tenantLogin(String raw, LoginRequest request, HttpServletRequest httpRequest) {
        int at = raw.indexOf('@');
        String localUsername = raw.substring(0, at);
        String schoolCode = raw.substring(at + 1).toLowerCase();

        if (localUsername.isBlank() || schoolCode.isBlank()) {
            throw new BadCredentialsException("Username must be in the form username@schoolcode");
        }

        School school = schoolRepository.findBySchoolCodeIgnoreCaseAndActiveTrue(schoolCode)
                .orElseThrow(() -> new TenantNotFoundException("Unknown or inactive school code: " + schoolCode));

        try {
            TenantContext.setCurrentSchool(schoolCode);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(localUsername, request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (!request.isForce() && sessionService.hasActiveSession(schoolCode, localUsername)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new LoginConflictResponse(
                        true,
                        "This account is already signed in elsewhere. Log out that session and continue here?"
                ));
            }

            String jti = UUID.randomUUID().toString();
            String token = jwtUtil.generateToken(userDetails, schoolCode, jti);
            sessionService.startSession(schoolCode, localUsername, jti, httpRequest.getRemoteAddr());

            return ResponseEntity.ok(new LoginResponse(
                    token, user.getUsername(), user.getFullName(), user.getRole().name(),
                    schoolCode, school.getSchoolName()
            ));
        } finally {
            TenantContext.clear();
        }
    }

    /** Plain-username login for the platform/super-admin account (manages schools). */
    private ResponseEntity<?> superAdminLogin(String username, String password) {
        SuperAdmin superAdmin = superAdminRepository.findByUsername(username)
                .filter(SuperAdmin::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(password, superAdmin.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String jti = UUID.randomUUID().toString();
        String token = jwtUtil.generateSuperAdminToken(superAdmin.getUsername(), jti);

        return ResponseEntity.ok(new LoginResponse(
                token, superAdmin.getUsername(), "Platform Administrator", "SUPER_ADMIN", null, null
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (!jwtUtil.isSuperAdmin(token)) {
                    String schoolCode = jwtUtil.extractSchoolCode(token);
                    String username = jwtUtil.extractUsername(token);
                    if (schoolCode != null && username != null) {
                        sessionService.endSession(schoolCode, username);
                    }
                }
            } catch (Exception ignored) {
                // token already invalid/expired - nothing to clean up
            }
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    // Only meant to be used by an already-authenticated school admin in a real deployment;
    // left open here for first-run setup convenience of additional staff logins within a school.
    // Because this hits the tenant (per-school) "users" table, it must be called with a valid
    // tenant Authorization header so JwtAuthFilter has already set the TenantContext for us.
    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole() != null ? request.getRole() : Role.STAFF);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}
