
package com.employeemanagement.controller;

import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.LoginRequest;
import com.employeemanagement.model.Users;
import com.employeemanagement.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            SecurityContextRepository securityContextRepository) {

        this.authService = authService;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        CurrentUser user = authService.authenticate(request);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid username or password"
            ));
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        user.username(),
                        null,
                        List.of(new SimpleGrantedAuthority(user.role()))
                );

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        sessionAuthenticationStrategy.onAuthentication(
                authentication,
                httpRequest,
                httpResponse
        );

        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(
                securityContext,
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, Object> body) {

        try {
            String username = (String) body.get("username");
            String password = (String) body.get("password");
            Object rawId = body.get("employeeId");

            if (rawId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "employeeId is required"
                ));
            }

            Long employeeId = Long.valueOf(rawId.toString());

            Users user = authService.register(
                    username,
                    password,
                    employeeId
            );

            return ResponseEntity.status(201).body(Map.of(
                    "message", "User registered successfully",
                    "userId", user.getId()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required"
            ));
        }

        try {
            String username = authentication.getName();

            Long userId = authService.getUserIdByUsername(username);

            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");

            authService.changePassword(
                    userId,
                    oldPassword,
                    newPassword
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Authentication required"
            ));
        }

        CurrentUser user = authService.getCurrentUser(
                authentication.getName()
        );

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "User account not found"
            ));
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request) {

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully"
        ));
    }
}
