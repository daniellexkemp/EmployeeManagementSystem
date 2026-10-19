
package com.employeemanagement.controller;

import com.employeemanagement.model.ContactUpdateRequest;
import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.Employee;
import com.employeemanagement.service.AuthService;
import com.employeemanagement.service.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuthService authService;

    public EmployeeController(
            EmployeeService employeeService,
            AuthService authService) {

        this.employeeService = employeeService;
        this.authService = authService;
    }

    private CurrentUser currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException(
                    "Authentication required."
            );
        }

        CurrentUser user = authService.getCurrentUser(
                authentication.getName()
        );

        if (user == null) {
            throw new AccessDeniedException(
                    "Authenticated user not found."
            );
        }

        return user;
    }

    @GetMapping
    public List<Employee> getAll(
            @RequestParam(required = false) String search) {

        return employeeService.searchEmployees(search);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<Employee> getMyProfile(
            Authentication authentication) {

        CurrentUser user = currentUser(authentication);

        if (user.employeeId() == null) {
            return ResponseEntity.notFound().build();
        }

        return employeeService
                .getEmployeeById(user.employeeId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/contact")
    public ResponseEntity<Employee> updateMyContact(
           @Valid @RequestBody ContactUpdateRequest request,
            Authentication authentication) {

        CurrentUser user = currentUser(authentication);

        if (user.employeeId() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Employee updated = employeeService
                    .updateContactInformation(
                            user.employeeId(),
                            request.email(),
                            request.phone()
                    );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Employee> create(
            @Valid @RequestBody Employee employee) {

        Employee saved = employeeService.createEmployee(employee);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @RequestBody Employee employee) {

        try {
            return ResponseEntity.ok(
                    employeeService.updateEmployee(id, employee)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Employee> deactivate(
            @PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    employeeService.deactivateEmployee(id)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
