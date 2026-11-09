package com.springboot.EasyPay.Dto.request;

import com.springboot.EasyPay.enums.LeaveType;
import com.springboot.EasyPay.enums.Status;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record LeaveRequestDto(
        @NotNull(message = "Leave type is required")
        LeaveType leaveType,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        @NotBlank(message = "Reason for leave cannot be blank")
        @Size(max = 500, message = "Reason cannot exceed 500 characters")
        String reason,

        @NotNull(message = "Status is required")
        Status status,

        boolean medicalCertificateSubmitted
) {
}
