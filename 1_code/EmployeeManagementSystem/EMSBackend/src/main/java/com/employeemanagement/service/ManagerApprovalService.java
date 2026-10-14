
package com.employeemanagement.service;

import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.TimeOffRequest;
import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.TimeOffRequestApprovalRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ManagerApprovalService {

    private final TimeOffRequestApprovalRepository requestRepository;
    private final EmployeeRepository employeeRepository;

    public ManagerApprovalService(
            TimeOffRequestApprovalRepository requestRepository,
            EmployeeRepository employeeRepository) {

        this.requestRepository = requestRepository;
        this.employeeRepository = employeeRepository;
    }

    private boolean isHrAdmin(CurrentUser user) {
        return "HR_ADMIN".equals(user.role())
                || "SYS_ADMIN".equals(user.role());
    }

    private boolean canReview(
            CurrentUser user,
            TimeOffRequest request) {

        Employee employee = request.getEmployee();

        // Nobody can review or decide their own request.
        if (Objects.equals(employee.getId(), user.employeeId())) {
            return false;
        }

        // HR and system administrators can review requests
        // from other employees across all departments.
        if (isHrAdmin(user)) {
            return true;
        }

        // Managers can only review requests from
        // other employees in their department.
        if (!"MANAGER".equals(user.role())) {
            return false;
        }

        return user.department() != null
                && !user.department().isBlank()
                && user.department().equalsIgnoreCase(
                        employee.getDepartment()
                );
    }

    @Transactional(readOnly = true)
    public List<TimeOffRequestResponse> getRequests(
            String status,
            CurrentUser user) {

        List<TimeOffRequest> requests =
                (status == null || status.equalsIgnoreCase("ALL"))
                        ? requestRepository.findAll()
                        : requestRepository.findByStatus(
                                status.toUpperCase()
                        );

        return requests.stream()
                .filter(request -> canReview(user, request))
                .map(TimeOffRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TimeOffRequestResponse> getRequest(
            Long id,
            CurrentUser user) {

        return requestRepository.findById(id)
                .map(request -> {
                    if (!canReview(user, request)) {
                        throw new AccessDeniedException(
                                "Not authorized to review this request."
                        );
                    }

                    return TimeOffRequestResponse.from(request);
                });
    }

    @Transactional
    public Optional<TimeOffRequestResponse> decide(
            Long id,
            String newStatus,
            CurrentUser user,
            String note) {

        if (!"APPROVED".equals(newStatus)
                && !"DENIED".equals(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid decision status."
            );
        }

        Optional<TimeOffRequest> found =
                requestRepository.findByIdForUpdate(id);

        if (found.isEmpty()) {
            return Optional.empty();
        }

        TimeOffRequest request = found.get();

        if (!canReview(user, request)) {
            throw new AccessDeniedException(
                    "Not authorized to decide this request."
            );
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException(
                    "Request " + id + " is already "
                            + request.getStatus()
            );
        }

        Employee manager = employeeRepository
                .findById(user.employeeId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated employee record not found."
                ));

        request.setStatus(newStatus);
        request.setDecidedBy(manager);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecisionNote(note);

        return Optional.of(
                TimeOffRequestResponse.from(
                        requestRepository.save(request)
                )
        );
    }
}
