
package com.employeemanagement.model;

public record ContactUpdateRequest(
        String email,
        String phone
) {
}
