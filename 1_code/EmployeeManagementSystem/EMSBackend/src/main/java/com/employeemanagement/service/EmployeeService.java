/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
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

    public Employee createEmployee(Employee employee) {
        if (employee.getEmploymentStatus() == null || employee.getEmploymentStatus().isBlank()) {
            employee.setEmploymentStatus("ACTIVE");
        }
        return repository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee updatedData) {
        return repository.findById(id).map(emp -> {
            emp.setFirstName(updatedData.getFirstName());
            emp.setLastName(updatedData.getLastName());
            emp.setEmail(updatedData.getEmail());
            emp.setPhone(updatedData.getPhone());
            emp.setDepartment(updatedData.getDepartment());
            emp.setJobTitle(updatedData.getJobTitle());
            if (updatedData.getEmploymentStatus() != null) {
                emp.setEmploymentStatus(updatedData.getEmploymentStatus());
            }
            return repository.save(emp);
        }).orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee deactivateEmployee(Long id) {
        return repository.findById(id).map(emp -> {
            emp.setEmploymentStatus("DEACTIVATED");
            return repository.save(emp);
        }).orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }
}