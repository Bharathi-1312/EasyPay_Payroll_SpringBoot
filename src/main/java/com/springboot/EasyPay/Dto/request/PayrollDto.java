package com.springboot.EasyPay.Dto.request;

import com.springboot.EasyPay.enums.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollDto(
        Long employeeId,
        LocalDate payPeriodStart,
        LocalDate payPeriodEnd,
        BigDecimal basicSalary,
        Long processedByHrId
) {
}
