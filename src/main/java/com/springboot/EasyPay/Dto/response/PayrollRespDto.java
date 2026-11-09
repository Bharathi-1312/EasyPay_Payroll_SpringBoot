package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PayrollRespDto (
        Long id,
        Long employeeId,
        LocalDate payPeriodStart,
        LocalDate payPeriodEnd,
        BigDecimal basicSalary,
        BigDecimal totalEarnings,
        BigDecimal totalDeductions,
        BigDecimal netSalary,
        PayrollStatus status,
        LocalDateTime processedAt,
        int paidLeaveDays,
        int unpaidLeaveDays,
        BigDecimal leaveDeduction
){
}
