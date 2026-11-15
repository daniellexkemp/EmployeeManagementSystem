package com.employeemanagement.controller;

import com.employeemanagement.model.AuditAction;
import com.employeemanagement.model.AuditLogEntry;
import com.employeemanagement.service.AuditService;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-log")
@PreAuthorize("hasAuthority('HR_ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        if (from != null && to != null && to.isBefore(from)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "The To date cannot be before the From date."
            ));
        }

        Page<AuditLogEntry> result =
                auditService.search(action, actor, from, to, page, size);

        return ResponseEntity.ok(Map.of(
                "items", result.getContent(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        ));
    }

    @GetMapping("/actions")
    public List<String> actions() {
        return Arrays.stream(AuditAction.values())
                .map(Enum::name)
                .toList();
    }
}
