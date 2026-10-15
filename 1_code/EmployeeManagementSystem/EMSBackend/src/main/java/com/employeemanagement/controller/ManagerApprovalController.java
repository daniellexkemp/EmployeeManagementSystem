
package com.employeemanagement.controller;

import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.DecisionRequest;
import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.service.AuthService;
import com.employeemanagement.service.ManagerApprovalService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@PreAuthorize("hasAnyAuthority('MANAGER', 'HR_ADMIN', 'SYS_ADMIN')")
public class ManagerApprovalController {

    private final ManagerApprovalService service;
    private final AuthService authService;

    public ManagerApprovalController(
            ManagerApprovalService service,
            AuthService authService) {

        this.service = service;
        this.authService = authService;
    }

    private CurrentUser currentUser(
            Authentication authentication) {

        if (authentication == null) {
            throw new IllegalStateException(
                    "Authentication required."
            );
        }

        CurrentUser user = authService.getCurrentUser(
                authentication.getName()
        );

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated user not found."
            );
        }

        return user;
    }

    @GetMapping
    public List<TimeOffRequestResponse> getRequests(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        return service.getRequests(
                status,
                currentUser(authentication)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeOffRequestResponse> getRequest(
            @PathVariable Long id,
            Authentication authentication) {

        return service.getRequest(
                id,
                currentUser(authentication)
        ).map(ResponseEntity::ok)
         .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TimeOffRequestResponse> approve(
            @PathVariable Long id,
            @RequestBody DecisionRequest decision,
            Authentication authentication) {

        return decide(
                id,
                "APPROVED",
                decision,
                authentication
        );
    }

    @PutMapping("/{id}/deny")
    public ResponseEntity<TimeOffRequestResponse> deny(
            @PathVariable Long id,
            @RequestBody DecisionRequest decision,
            Authentication authentication) {

        return decide(
                id,
                "DENIED",
                decision,
                authentication
        );
    }

    private ResponseEntity<TimeOffRequestResponse> decide(
            Long id,
            String status,
            DecisionRequest decision,
            Authentication authentication) {

        return service.decide(
                id,
                status,
                currentUser(authentication),
                decision.decisionNote()
        ).map(ResponseEntity::ok)
         .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler({
            IllegalStateException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Map<String, String>> handleBadDecision(
            RuntimeException e) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }
}
