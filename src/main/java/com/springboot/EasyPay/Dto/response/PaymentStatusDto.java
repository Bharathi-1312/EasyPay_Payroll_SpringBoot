package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.PayrollStatus;

public record PaymentStatusDto(Long payrollId,
                               PayrollStatus status,
                               boolean paid) {
}
