package com.employeemanagement.service;

import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.TimeOffRequest;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.TimeOffRequestApprovalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerApprovalService {

    private final TimeOffRequestApprovalRepository requestRepository;
    private final EmployeeRepository employeeRepository;

    public ManagerApprovalService(TimeOffRequestApprovalRepository requestRepository,
                                  EmployeeRepository employeeRepository) {
        this.requestRepository = requestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<TimeOffRequestResponse> getRequests(String status) {
        List<TimeOffRequest> requests = (status == null || status.equalsIgnoreCase("ALL"))
                ? requestRepository.findAll()
                : requestRepository.findByStatus(status.toUpperCase());
        return requests.stream().map(TimeOffRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Optional<TimeOffRequestResponse> getRequest(Long id) {
        return requestRepository.findById(id).map(TimeOffRequestResponse::from);
    }

    @Transactional
    public Optional<TimeOffRequestResponse> decide(Long id, String newStatus, Long decidedById, String note) {
        Optional<TimeOffRequest> found = requestRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        TimeOffRequest request = found.get();
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request " + id + " is already " + request.getStatus());
        }
        Employee manager = employeeRepository.findById(decidedById)
                .orElseThrow(() -> new IllegalArgumentException("Manager " + decidedById + " not found"));

        request.setStatus(newStatus);
        request.setDecidedBy(manager);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecisionNote(note);
        return Optional.of(TimeOffRequestResponse.from(requestRepository.save(request)));
    }
}
