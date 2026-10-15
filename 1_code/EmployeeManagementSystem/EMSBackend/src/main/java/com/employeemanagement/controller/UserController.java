
package com.employeemanagement.controller;

import com.employeemanagement.model.Users;
import com.employeemanagement.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("EMPLOYEE", "MANAGER", "HR_ADMIN");

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<?> getUsers() {

        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "employeeId", user.getEmployee().getId(),
                        "firstName", user.getEmployee().getFirstName(),
                        "lastName", user.getEmployee().getLastName(),
                        "role", user.getRole(),
                        "enabled", user.isEnabled()
                ))
                .toList();

        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<?> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String role = body.get("role");

        if (role == null || !ALLOWED_ROLES.contains(role)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role"));
        }

        Users user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getUsername().equals(authentication.getName())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "error",
                            "You cannot change your own role"
                    ));
        }

        if ("SYS_ADMIN".equals(user.getRole())) {
    return ResponseEntity.status(403)
            .body(Map.of(
                    "error",
                    "System administrator roles cannot be modified here"
            ));
        }

        user.setRole(role);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User role updated successfully",
                "userId", user.getId(),
                "role", user.getRole()
        ));
    }
}
