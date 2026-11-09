package com.springboot.EasyPay.Dto.request;

import com.springboot.EasyPay.enums.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HRDto(
        @NotBlank(message = "Full name is required")
        @NotNull(message = "Full name is required")
        String name,
        @NotBlank(message = "Email is required")
        @NotNull(message = "Email is required")
        @Email(message = "Email seems invalid")
        String email,

        @NotBlank(message = "Phone is required")
        @NotNull(message = "Phone is required")
        @Size(min = 10, max = 10, message = "contact should be 10 digit mobile number")
        String phone,

        @NotBlank(message = "Phone is required")
        @NotNull(message = "Phone is required")
        String address,

        @NotNull(message = "Department is required")
        Department department,
        @NotBlank
        @NotNull
        @Size(min = 3, max = 15)
        String username,

        @NotBlank
        @NotNull
        String password
) {
}
