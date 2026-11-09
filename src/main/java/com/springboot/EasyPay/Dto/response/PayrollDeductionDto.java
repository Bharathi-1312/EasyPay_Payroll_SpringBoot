package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.PayrollStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayrollDeductionDto (Long payrollId,
                                   LocalDate periodStart,
                                   BigDecimal totalDeductions,
                                   PayrollStatus status){
}
