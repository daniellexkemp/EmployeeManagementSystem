package com.employeemanagement.model;

import java.time.LocalDate;

public record TimeOffSubmissionRequest(
        Long employeeId,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason) {
}