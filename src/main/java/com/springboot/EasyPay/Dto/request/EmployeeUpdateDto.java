package com.springboot.EasyPay.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeUpdateDto (@NotBlank(message = "Name is required")
                                 @NotNull(message = "Name is required")
                                 String fullname,

                                 @NotBlank(message = "Phone is required")
                                 @NotNull(message = "Phone is required")
                                 @Size(min = 10, max = 10, message = "contact should be 10 digit mobile number")
                                 String phone,

                                 @NotBlank(message = "Phone is required")
                                 @NotNull(message = "Phone is required")
                                 String address){
}
