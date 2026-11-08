
package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;
    private final UserRepository userRepository;
    
    public EmployeeService(EmployeeRepository repository, UserRepository userRepository) {
    this.repository = repository;
    this.userRepository = userRepository;   
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
   public Employee createEmployee(Employee employee, Long supervisorId) {
        if (employee.getEmploymentStatus() == null
                || employee.getEmploymentStatus().isBlank()) {

            employee.setEmploymentStatus("ACTIVE");
        }
if (supervisorId != null) {
    Employee supervisor = repository.findById(supervisorId)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Supervisor not found: " + supervisorId));
    
boolean validSupervisor = userRepository
        .findByRoleAndEnabledTrue("MANAGER")
        .stream()
        .anyMatch(user -> user.getEmployee() != null
                && supervisorId.equals(user.getEmployee().getId()));

if (!validSupervisor) {
    throw new IllegalArgumentException(
            "Selected supervisor must have an enabled MANAGER account.");
}

    employee.setManager(supervisor);
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
    public List<Employee> getEmployeesByManager(Long managerId) {
    return repository.findByManager_Id(managerId);
}

public List<Employee> getEligibleSupervisors() {
    return userRepository.findByRoleAndEnabledTrue("MANAGER")
            .stream()
            .map(user -> user.getEmployee())
            .filter(employee -> employee != null)
            .toList();
}
    
}
