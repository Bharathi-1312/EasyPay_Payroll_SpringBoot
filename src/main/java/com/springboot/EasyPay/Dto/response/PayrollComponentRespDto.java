package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;


public record PayrollComponentRespDto(
        Long id,
        Long payrollId,
        ComponentName componentName,
        ComponentType componentType,
        BigDecimal amountPercentage,
        String description
) {}