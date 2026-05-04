package com.snackOverFlow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterCustomerRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20)
    private String phone;
}

