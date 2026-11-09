package com.springboot.EasyPay.Dto.response;

import java.math.BigDecimal;

public record MonthlyPaymentDto(int year,
                                int month,
                                long payrollCount,
                                BigDecimal totalEarnings,
                                BigDecimal totalDeductions,
                                BigDecimal totalNetSalary) {
}
