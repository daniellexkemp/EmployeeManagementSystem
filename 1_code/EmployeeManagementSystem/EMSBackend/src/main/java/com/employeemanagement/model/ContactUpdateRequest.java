
package com.employeemanagement.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ContactUpdateRequest(
@Email(message = "Please enter a valid email address"
)
String email,

@Size(max = 25, message = "Phone number cannot exceed 25 characters")
String phone
        ) {
    
}