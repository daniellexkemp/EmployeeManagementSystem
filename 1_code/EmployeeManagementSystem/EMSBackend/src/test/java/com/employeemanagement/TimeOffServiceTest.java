
package com.employeemanagement;

import com.employeemanagement.model.Employee;
import com.employeemanagement.model.TimeOffRequest;
import com.employeemanagement.model.TimeOffRequestResponse;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.TimeOffRepository;
import com.employeemanagement.service.TimeOffService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TimeOffServiceTest {

    private TimeOffRepository timeOffRepository;
    private EmployeeRepository employeeRepository;
    private TimeOffService timeOffService;

    private Employee employee;

    @BeforeEach
    void setup() {
        timeOffRepository = mock(TimeOffRepository.class);
        employeeRepository = mock(EmployeeRepository.class);

        timeOffService = new TimeOffService(
                timeOffRepository, employeeRepository);

        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("Alex");
        employee.setLastName("Employee");
    }

    @Test
    void validTimeOffRequestIsSaved() {
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(timeOffRepository.save(any(TimeOffRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TimeOffRequestResponse response = timeOffService.submitRequest(
                1L,
                "Vacation",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                "Family trip"
        );

        assertEquals("PENDING", response.status());
        assertEquals("Vacation", response.leaveType());
        assertEquals("Alex Employee", response.employeeName());
        assertEquals(new BigDecimal("3"), response.daysRequested());

        verify(timeOffRepository).save(any(TimeOffRequest.class));
    }

    @Test
    void missingEmployeeIdIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.submitRequest(
                        null, "Vacation",
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 12),
                        "Family trip"
                ));

        verifyNoInteractions(timeOffRepository);
    }

    @Test
    void missingLeaveTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.submitRequest(
                        1L, "",
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 12),
                        "Family trip"
                ));

        verifyNoInteractions(timeOffRepository);
    }

    @Test
    void missingDatesAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.submitRequest(
                        1L, "Vacation",
                        null,
                        LocalDate.of(2026, 11, 12),
                        "Family trip"
                ));

        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.submitRequest(
                        1L, "Vacation",
                        LocalDate.of(2026, 11, 10),
                        null,
                        "Family trip"
                ));

        verifyNoInteractions(timeOffRepository);
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.submitRequest(
                        1L, "Vacation",
                        LocalDate.of(2026, 11, 12),
                        LocalDate.of(2026, 11, 10),
                        "Family trip"
                ));

        verifyNoInteractions(timeOffRepository);
    }

    @Test
    void nonexistentEmployeeIsRejected() {
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.submitRequest(
                        99L, "Vacation",
                        LocalDate.of(2026, 11, 10),
                        LocalDate.of(2026, 11, 12),
                        "Family trip"
                ));

        verify(timeOffRepository, never())
                .save(any(TimeOffRequest.class));
    }

    @Test
    void singleDayRequestCountsAsOneDay() {
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(timeOffRepository.save(any(TimeOffRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate date = LocalDate.of(2026, 11, 10);

        TimeOffRequestResponse response = timeOffService.submitRequest(
                1L, "Personal", date, date, "Appointment"
        );

        assertEquals(new BigDecimal("1"), response.daysRequested());
    }

    @Test
    void employeeCanRetrieveTheirRequests() {
        when(employeeRepository.existsById(1L))
                .thenReturn(true);

        TimeOffRequest request = new TimeOffRequest(
                employee,
                "Vacation",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                "Family trip"
        );

        request.setDaysRequested(new BigDecimal("3"));

        when(timeOffRepository.findByEmployeeIdOrderBySubmittedAtDesc(1L))
                .thenReturn(List.of(request));

        List<TimeOffRequestResponse> results =
                timeOffService.getRequestsForEmployee(1L);

        assertEquals(1, results.size());
        assertEquals("Vacation", results.get(0).leaveType());
        assertEquals(1L, results.get(0).employeeId());
    }

    @Test
    void nonexistentEmployeeCannotRetrieveRequests() {
        when(employeeRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                timeOffService.getRequestsForEmployee(99L));

        verifyNoInteractions(timeOffRepository);
    }
}
