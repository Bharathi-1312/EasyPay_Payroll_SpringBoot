package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.PayrollStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollEarningsDto(Long payrollId,
                                 LocalDate periodStart,
                                 BigDecimal basicSalary,
                                 BigDecimal totalEarnings,
                                 PayrollStatus status) {
}
