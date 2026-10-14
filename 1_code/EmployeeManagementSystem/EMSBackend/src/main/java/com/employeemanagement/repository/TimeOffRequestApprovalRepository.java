
package com.employeemanagement.repository;

import com.employeemanagement.model.TimeOffRequest;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TimeOffRequestApprovalRepository
        extends JpaRepository<TimeOffRequest, Long> {

    List<TimeOffRequest> findByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM TimeOffRequest r WHERE r.requestId = :id")
    Optional<TimeOffRequest> findByIdForUpdate(
            @Param("id") Long id
    );
}
