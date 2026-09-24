package com.employeemanagement.controller;

import com.employeemanagement.model.DecisionRequest;
import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.service.ManagerApprovalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/approvals")
public class ManagerApprovalController {

    private final ManagerApprovalService service;

    public ManagerApprovalController(ManagerApprovalService service) {
        this.service = service;
    }

    @GetMapping
    public List<TimeOffRequestResponse> getRequests(@RequestParam(required = false) String status) {
        return service.getRequests(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeOffRequestResponse> getRequest(@PathVariable Long id) {
        return service.getRequest(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TimeOffRequestResponse> approve(@PathVariable Long id, @RequestBody DecisionRequest decision) {
        return decide(id, "APPROVED", decision);
    }

    @PutMapping("/{id}/deny")
    public ResponseEntity<TimeOffRequestResponse> deny(@PathVariable Long id, @RequestBody DecisionRequest decision) {
        return decide(id, "DENIED", decision);
    }

    private ResponseEntity<TimeOffRequestResponse> decide(Long id, String status, DecisionRequest decision) {
        return service.decide(id, status, decision.decidedBy(), decision.decisionNote())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleBadDecision(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
