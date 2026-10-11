package com.employeemanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "users")
public class Users {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;

    /** BCrypt hash. A plaintext password is never stored. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** EMPLOYEE, MANAGER, HR_ADMIN or SYS_ADMIN. */
    @Column(name = "role", nullable = false, length = 30)
    private String role = "EMPLOYEE";

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
};

 