package com.springboot.EasyPay.Dto.request;

import com.springboot.EasyPay.enums.Department;
import com.springboot.EasyPay.enums.Designation;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeDto (
        @NotBlank(message = "Full name is required")
        @NotNull(message = "Full name is required")
        String fullName,
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

        @NotNull(message = "Designation is required")
        Designation designation,

        @NotNull(message = "Department is required")
        Department department,

        @NotNull(message = "Joining date is required")
        LocalDate joiningDate,

        @NotNull(message = "Basic salary is required")
        @PositiveOrZero(message = "Salary must be positive")
        double basicSalary,

        @NotBlank
        @NotNull
        @Size(min = 3, max = 15)
        String username,

        @NotBlank
        @NotNull
        String password,

        @NotNull(message = "Annual package is required")
        @Positive(message = "Annual package must be positive")
        BigDecimal annualPackage
){

}
