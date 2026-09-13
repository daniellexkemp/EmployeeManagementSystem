/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.employeemanagement.service;

import com.employeemanagement.model.Employee;
import com.employeemanagement.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
/**
 *
 * @author NerdyGirl44
 */
@Service
public class EmployeeService {
 private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;    
}
public Optional<Employee> getEmployeeById(Long id) {
    return employeeRepository.findById(id);
}  
public Employee updateEmployee(Employee employee) {
    return employeeRepository.save(employee);
}
}
