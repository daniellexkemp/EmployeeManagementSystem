
package com.employeemanagement.controller;

import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.model.TimeOffSubmissionRequest;
import com.employeemanagement.service.AuthService;
import com.employeemanagement.service.TimeOffService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timeoff")
public class TimeOffController {

    private final TimeOffService service;
    private final AuthService authService;

    public TimeOffController(
            TimeOffService service,
            AuthService authService) {

        this.service = service;
        this.authService = authService;
    }

    private Long getAuthenticatedEmployeeId(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "Authentication required.");
        }

        CurrentUser user = authService.getCurrentUser(
                authentication.getName()
        );

        if (user == null || user.employeeId() == null) {
            throw new IllegalStateException(
                    "No active employee account is associated with this user.");
        }

        return user.employeeId();
    }

    @PostMapping
    public ResponseEntity<TimeOffRequestResponse> submitRequest(
            @RequestBody TimeOffSubmissionRequest request,
            Authentication authentication) {

        Long employeeId =
                getAuthenticatedEmployeeId(authentication);

        TimeOffRequestResponse response = service.submitRequest(
                employeeId,
                request.leaveType(),
                request.startDate(),
                request.endDate(),
                request.reason()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mine")
    public List<TimeOffRequestResponse> getMyRequests(
            Authentication authentication) {

        Long employeeId =
                getAuthenticatedEmployeeId(authentication);

        return service.getRequestsForEmployee(employeeId);
    }

    @GetMapping("/employee/{employeeId}")
    public List<TimeOffRequestResponse> getEmployeeRequests(
            @PathVariable Long employeeId,
            Authentication authentication) {

        Long authenticatedEmployeeId =
                getAuthenticatedEmployeeId(authentication);

        if (!authenticatedEmployeeId.equals(employeeId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot view another employee's time-off requests."
            );
        }

        return service.getRequestsForEmployee(employeeId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException e) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(
            IllegalStateException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
    }
}
