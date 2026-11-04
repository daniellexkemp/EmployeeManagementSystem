package com.employeemanagement.repository;

import com.employeemanagement.model.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeOffRepository extends JpaRepository<TimeOffRequest, Long> {

    // Retrieve requests submitted by a specific employee.
    List<TimeOffRequest> findByEmployeeIdOrderBySubmittedAtDesc(Long employeeId);

    // Retrieve all requests for HR time-off reporting.
    List<TimeOffRequest> findAllByOrderBySubmittedAtDesc();
}
