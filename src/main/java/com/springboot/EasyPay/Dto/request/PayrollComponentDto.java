package com.springboot.EasyPay.Dto.request;

import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PayrollComponentDto(
                                  ComponentName componentName,
                                  ComponentType componentType,
                                  BigDecimal amountPercentage,
                                  String description){
}
