
package com.employeemanagement.service;

import com.employeemanagement.model.CurrentUser;
import com.employeemanagement.model.Employee;
import com.employeemanagement.model.LoginRequest;
import com.employeemanagement.model.Users;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository) {

        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public CurrentUser authenticate(LoginRequest request) {

        if (request == null
                || request.username() == null
                || request.password() == null) {
            return null;
        }

        Optional<Users> userOpt =
                userRepository.findByUsername(request.username());

        if (userOpt.isEmpty()) {
            return null;
        }

        Users user = userOpt.get();

        if (!user.isEnabled()
                || !passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash())) {
            return null;
        }

        return toCurrentUser(user);
    }

    @Transactional(readOnly = true)
    public CurrentUser getCurrentUser(String username) {

        if (username == null) {
            return null;
        }

        return userRepository.findByUsername(username)
                .filter(Users::isEnabled)
                .map(this::toCurrentUser)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Long getUserIdByUsername(String username) {

        return userRepository.findByUsername(username)
                .filter(Users::isEnabled)
                .map(Users::getId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account not found"));
    }

    @Transactional
    public Users register(
            String username,
            String plainPassword,
            Long employeeId) {

        if (username == null
                || username.isBlank()
                || plainPassword == null
                || plainPassword.length() < 6) {

            throw new IllegalArgumentException(
                    "Username and a password of at least "
                    + "6 characters are required");
        }

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "Employee ID is required");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                    "Username already exists: " + username);
        }
        if (userRepository.existsByEmployee_Id(employeeId)) {
    throw new IllegalArgumentException(
            "Employee " + employeeId
            + " already has a user account");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Employee " + employeeId + " not found"));

        Users user = new Users();

        user.setEmployee(employee);
        user.setUsername(username);
        user.setPasswordHash(
                passwordEncoder.encode(plainPassword));
        user.setRole("EMPLOYEE");
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(
            Long userId,
            String oldPassword,
            String newPassword) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with id: " + userId));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException(
                    "User account is disabled");
        }

        if (oldPassword == null
                || !passwordEncoder.matches(
                        oldPassword,
                        user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        if (newPassword == null
                || newPassword.length() < 6) {

            throw new IllegalArgumentException(
                    "New password must be at least 6 characters");
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    private CurrentUser toCurrentUser(Users user) {

        Employee employee = user.getEmployee();

        return new CurrentUser(
                user.getId(),
                employee.getId(),
                user.getUsername(),
                user.getRole(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getJobTitle()
        );
    }
}
