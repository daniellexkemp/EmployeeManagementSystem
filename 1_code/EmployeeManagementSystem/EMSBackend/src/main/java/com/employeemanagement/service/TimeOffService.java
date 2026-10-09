package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.TimeOffRequest;
import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.TimeOffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TimeOffService {

    private final TimeOffRepository timeOffRepository;
    private final EmployeeRepository employeeRepository;

    public TimeOffService(TimeOffRepository timeOffRepository,
                          EmployeeRepository employeeRepository) {
        this.timeOffRepository = timeOffRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public TimeOffRequestResponse submitRequest(Long employeeId,
                                                String leaveType,
                                                java.time.LocalDate startDate,
                                                java.time.LocalDate endDate,
                                                String reason) {

        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }

        if (leaveType == null || leaveType.isBlank()) {
            throw new IllegalArgumentException("Leave type is required.");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date.");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee " + employeeId + " not found."));

        TimeOffRequest request = new TimeOffRequest(
                employee,
                leaveType,
                startDate,
                endDate,
                reason
        );

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        request.setDaysRequested(BigDecimal.valueOf(days));

        return TimeOffRequestResponse.from(timeOffRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<TimeOffRequestResponse> getRequestsForEmployee(Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            throw new IllegalArgumentException(
                    "Employee " + employeeId + " not found.");
        }

        return timeOffRepository
                .findByEmployeeIdOrderBySubmittedAtDesc(employeeId)
                .stream()
                .map(TimeOffRequestResponse::from)
                .toList();
    }
}