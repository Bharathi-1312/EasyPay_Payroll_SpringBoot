package com.springboot.EasyPay.mapper;

import com.springboot.EasyPay.Dto.request.PayrollComponentDto;
import com.springboot.EasyPay.Dto.response.PayrollComponentRespDto;
import com.springboot.EasyPay.model.PayrollComponent;
import org.springframework.stereotype.Component;

@Component
public class PayrollComponentMapper {

    public static PayrollComponent convertDtoToEntity(
            PayrollComponentDto dto) {

        PayrollComponent component = new PayrollComponent();

        component.setComponentName(
               dto.componentName()
        );

        component.setComponentType(
                dto.componentType()
        );

        component.setAmountPercentage(dto.amountPercentage());
        component.setDescription(dto.description());

        return component;
    }

    public static PayrollComponentRespDto convertEntityToDto(
            PayrollComponent component) {

        return new PayrollComponentRespDto(
                component.getId(),
                component.getPayroll().getId(),
                component.getComponentName(),
                component.getComponentType(),
                component.getAmountPercentage(),
                component.getDescription()
        );
    }
}