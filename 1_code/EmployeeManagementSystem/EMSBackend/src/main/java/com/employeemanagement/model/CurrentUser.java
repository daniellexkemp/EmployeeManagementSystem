package com.employeemanagement.model;

/*Represents the currently authenticated user*/

public record CurrentUser(
        Long userId,
        Long employeeId,
        String username,
        String role,
        String firstName,
        String lastName,
        String email,
        String department,
        String jobTitle
) {
}
