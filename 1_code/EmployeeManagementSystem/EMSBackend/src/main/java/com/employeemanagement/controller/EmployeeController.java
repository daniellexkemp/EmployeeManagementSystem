package com.employeemanagement.controller;

import com.employeemanagement.model.AuditAction;
import com.employeemanagement.model.ContactUpdateRequest;
import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.Employee;
import com.employeemanagement.service.AuditService;
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
    private final AuditService auditService;

    public EmployeeController(
            EmployeeService employeeService,
            AuthService authService,
            AuditService auditService) {

        this.employeeService = employeeService;
        this.authService = authService;
        this.auditService = auditService;
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
@PreAuthorize("hasAnyAuthority('HR_ADMIN, 'SYS_ADMIN', 'MANAGER')")
public List<Employee> getAll(
        @RequestParam(required = false) String search,
        Authentication authentication) {
    CurrentUser user = currentUser(authentication);

    if ("MANAGER".equals(user.role())) {
        if (user.employeeId() == null) {
            throw new AccessDeniedException(
                "Manager is not linked to an employee record."
            );
        }

        List<Employee> supervisedEmployees =
                employeeService.getEmployeesByManager(user.employeeId());

        if (search == null || search.isBlank()) {
            return supervisedEmployees;
        }

        String query = search.trim().toLowerCase();

        return supervisedEmployees.stream()
                .filter(e ->
                    (e.getFirstName() != null &&
                     e.getFirstName().toLowerCase().contains(query)) ||
                    (e.getLastName() != null &&
                     e.getLastName().toLowerCase().contains(query)) ||
                    (e.getEmail() != null &&
                     e.getEmail().toLowerCase().contains(query)) ||
                    (e.getDepartment() != null &&
                     e.getDepartment().toLowerCase().contains(query))
                )
                .toList();
    }

    return employeeService.searchEmployees(search);
}
@GetMapping("/supervisors")
@PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
public List<Employee> getSupervisor() {
    return employeeService.getEligibleSupervisors();
}
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('HR_ADMIN, 'SYS_ADMIN', 'MANAGER')")
public ResponseEntity<Employee> getById(
        @PathVariable Long id,
        Authentication authentication) {

    CurrentUser user = currentUser(authentication);

    if ("MANAGER".equals(user.role())) {
        if (user.employeeId() == null) {
            throw new AccessDeniedException(
                "Manager is not linked to an employee record."
            );
        }

        Employee employee = employeeService.getEmployeeById(id)
                .orElse(null);

        if (employee == null) {
            return ResponseEntity.notFound().build();
        }

        Employee manager = employee.getManager();

        if (manager == null ||
                !user.employeeId().equals(manager.getId())) {
            throw new AccessDeniedException(
                "You are not authorized to view this employee."
            );
        }

        return ResponseEntity.ok(employee);

    }
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

            auditService.record(
                    AuditAction.UPDATE_CONTACT,
                    AuditService.describeEmployee(updated),
                    "Email and phone updated"
            );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
   public ResponseEntity<Employee> create(
        @Valid @RequestBody Employee employee,
        @RequestParam(required = false) Long supervisorId) {

        Employee saved = employeeService.createEmployee(employee, supervisorId);

        auditService.record(
                AuditAction.CREATE_EMPLOYEE,
                AuditService.describeEmployee(saved),
                supervisorId == null
                        ? null
                        : "Supervisor: Employee #" + supervisorId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Employee> update(
            @PathVariable Long id,
            @Valid @RequestBody Employee employee) {

        try {
            Employee updated = employeeService.updateEmployee(id, employee);

            auditService.record(
                    AuditAction.UPDATE_EMPLOYEE,
                    AuditService.describeEmployee(updated),
                    "Status: " + updated.getEmploymentStatus()
            );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Employee> deactivate(
            @PathVariable Long id) {

        try {
            Employee updated = employeeService.deactivateEmployee(id);

            auditService.record(
                    AuditAction.DEACTIVATE_EMPLOYEE,
                    AuditService.describeEmployee(updated),
                    null
            );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'SYS_ADMIN')")
    public ResponseEntity<Employee> reactivate(
            @PathVariable Long id) {

        try {
            Employee updated = employeeService.reactivateEmployee(id);

            auditService.record(
                    AuditAction.REACTIVATE_EMPLOYEE,
                    AuditService.describeEmployee(updated),
                    null
            );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}