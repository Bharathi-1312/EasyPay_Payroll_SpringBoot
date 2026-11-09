package com.springboot.EasyPay.Dto.request;

import com.springboot.EasyPay.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LeaveReviewDto(
        @NotNull(message = "Review status is required")
        Status status, // Typically APPROVED or REJECTED

        @NotBlank(message = "Review comments cannot be blank")
        @Size(max = 500, message = "Comments cannot exceed 500 characters")
        String reviewComments
) {
}
