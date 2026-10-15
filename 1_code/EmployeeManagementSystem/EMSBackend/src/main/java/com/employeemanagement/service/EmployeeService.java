
package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> getAllEmployees() {
        return repository.findAll();
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return repository.findById(id);
    }

    public List<Employee> searchEmployees(String query) {
        if (query == null || query.isBlank()) {
            return getAllEmployees();
        }

        return repository.searchEmployees(query.trim());
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employee.getEmploymentStatus() == null
                || employee.getEmploymentStatus().isBlank()) {

            employee.setEmploymentStatus("ACTIVE");
        }

        return repository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updatedData) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found with id: " + id
                ));

        employee.setFirstName(updatedData.getFirstName());
        employee.setLastName(updatedData.getLastName());
        employee.setEmail(updatedData.getEmail());
        employee.setPhone(updatedData.getPhone());
        employee.setDepartment(updatedData.getDepartment());
        employee.setJobTitle(updatedData.getJobTitle());

        if (updatedData.getEmploymentStatus() != null) {
            employee.setEmploymentStatus(
                    updatedData.getEmploymentStatus()
            );
        }

        return repository.save(employee);
    }

    @Transactional
    public Employee updateContactInformation(
            Long employeeId,
            String email,
            String phone) {

        if (email == null || email.isBlank()
                || phone == null || phone.isBlank()) {

            throw new IllegalArgumentException(
                    "Email and phone are required."
            );
        }

        Employee employee = repository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found with id: " + employeeId
                ));

        employee.setEmail(email.trim());
        employee.setPhone(phone.trim());

        return repository.save(employee);
    }

    @Transactional
    public Employee deactivateEmployee(Long id) {
        Employee employee = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Employee not found with id: " + id
                ));

        employee.setEmploymentStatus("DEACTIVATED");

        return repository.save(employee);
    }
}
