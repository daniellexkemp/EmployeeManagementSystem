/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.employeemanagement.repository;
import com.employeemanagement.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 *
 * @author NerdyGirl44
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
}