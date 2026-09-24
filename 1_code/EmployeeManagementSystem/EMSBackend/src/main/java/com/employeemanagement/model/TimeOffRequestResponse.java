package com.employeemanagement.model;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeOffRequestResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal daysRequested,
        String reason,
        String status,
        LocalDateTime submittedAt,
        Long decidedBy,
        LocalDateTime decidedAt,
        String decisionNote) {

    public static TimeOffRequestResponse from(TimeOffRequest request) {
        Employee employee = request.getEmployee();
        Employee decider = request.getDecidedBy();
        return new TimeOffRequestResponse(
                request.getRequestId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                request.getLeaveType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDaysRequested(),
                request.getReason(),
                request.getStatus(),
                request.getSubmittedAt(),
                decider == null ? null : decider.getId(),
                request.getDecidedAt(),
                request.getDecisionNote());
    }
}
