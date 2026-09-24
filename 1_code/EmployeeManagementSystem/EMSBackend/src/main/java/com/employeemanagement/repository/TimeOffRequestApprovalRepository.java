package com.employeemanagement.repository;

import com.employeemanagement.model.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeOffRequestApprovalRepository extends JpaRepository<TimeOffRequest, Long> {
    List<TimeOffRequest> findByStatus(String status);
}
