package com.employeemanagement.controller;

import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.LoginRequest;
import com.employeemanagement.model.Users;
import com.employeemanagement.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Authenticates a user and returns their profile if credentials are valid.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        CurrentUser user = authService.authenticate(request);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid username or password"
            ));
        }

        return ResponseEntity.ok(user);
    }

    /**
     * POST /api/auth/register
     * Registers a new user account linked to an existing employee record.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            String username = (String) body.get("username");
            String password = (String) body.get("password");
            Object rawId = body.get("employeeId");
            if (rawId == null) {
                return ResponseEntity.status(400).body(Map.of("error", "employeeId is required"));
            }
            Long employeeId = Long.valueOf(rawId.toString());

            Users user = authService.register(username, password, employeeId);

            return ResponseEntity.status(201).body(Map.of(
                    "message", "User registered successfully",
                    "userId", user.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * POST /api/auth/change-password
     * Allows an authenticated user to change their password.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        try {
            Long userId = Long.valueOf(body.get("userId"));
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");

            authService.changePassword(userId, oldPassword, newPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Password change failed: " + e.getMessage()
            ));
        }
    }
}
