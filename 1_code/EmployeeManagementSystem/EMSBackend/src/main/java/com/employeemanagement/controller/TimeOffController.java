package com.employeemanagement.controller;

import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.model.TimeOffSubmissionRequest;
import com.employeemanagement.service.TimeOffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/timeoff")
public class TimeOffController {

    private final TimeOffService service;

    public TimeOffController(TimeOffService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TimeOffRequestResponse> submitRequest(
            @RequestBody TimeOffSubmissionRequest request) {

        TimeOffRequestResponse response = service.submitRequest(
                request.employeeId(),
                request.leaveType(),
                request.startDate(),
                request.endDate(),
                request.reason()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/employee/{employeeId}")
    public List<TimeOffRequestResponse> getEmployeeRequests(
            @PathVariable Long employeeId) {

        return service.getRequestsForEmployee(employeeId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException e) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}
