package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;

public record PayrollComponentInfoDto(ComponentName name, ComponentType type, double amount, String description) {
}
