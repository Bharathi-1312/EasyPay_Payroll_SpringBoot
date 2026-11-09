package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.PayrollComponentDto;
import com.springboot.EasyPay.Dto.response.PayrollComponentRespDto;
import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.mapper.PayrollComponentMapper;
import com.springboot.EasyPay.model.Payroll;
import com.springboot.EasyPay.model.PayrollComponent;
import com.springboot.EasyPay.repository.PayrollComponentRepository;
import com.springboot.EasyPay.repository.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PayrollComponentService {

    private final PayrollComponentRepository componentRepository;
    private final PayrollRepository payrollRepository;

    //@Transactional
    public PayrollComponentRespDto addComponent(Long payrollId,
                                                PayrollComponentDto dto) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new RuntimeException("Payroll not found"));

        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException(
                    "Components can only be added to draft payroll");
        }

        if (dto.amountPercentage() == null
                || dto.amountPercentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Amount must be zero or greater");
        }

        PayrollComponent component =
                PayrollComponentMapper.convertDtoToEntity(dto);

        component.setPayroll(payroll);

        PayrollComponent saved =
                componentRepository.save(component);

        return PayrollComponentMapper.convertEntityToDto(saved);
    }

    //@Transactional(readOnly = true)
    public List<PayrollComponentRespDto> getComponents(
            Long payrollId) {

        if (!payrollRepository.existsById(payrollId)) {
            throw new RuntimeException("Payroll not found");
        }

        return componentRepository.findByPayrollId(payrollId)
                .stream()
                .map(PayrollComponentMapper::convertEntityToDto)
                .toList();
    }

    //@Transactional
    public void deleteComponent(Long componentId) {

        PayrollComponent component =
                componentRepository.findById(componentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payroll component not found"));

        if (component.getPayroll().getStatus()
                != PayrollStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft payroll components can be deleted");
        }

        componentRepository.delete(component);
    }

    // API 15: Update a component in a DRAFT payroll
    //@Transactional
    public PayrollComponentRespDto updateComponent(
            Long componentId, PayrollComponentDto dto) {

        PayrollComponent component =
                componentRepository.findById(componentId)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Component not found"));

        if (component.getPayroll().getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft components can be updated");
        }

        if (dto.amountPercentage() == null
                || dto.amountPercentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        component.setComponentName(
                ComponentName.valueOf(String.valueOf(dto.componentName())));

        component.setComponentType(
                ComponentType.valueOf(String.valueOf(dto.componentType())));

        component.setAmountPercentage(dto.amountPercentage());
        component.setDescription(dto.description());

        return PayrollComponentMapper.convertEntityToDto(
                componentRepository.save(component));
    }

    // API 16: Filter components by type
    //@Transactional(readOnly = true)
    public List<PayrollComponentRespDto> getComponentsByType(
            ComponentType type) {

        return componentRepository.findByComponentType(type)
                .stream()
                .map(PayrollComponentMapper::convertEntityToDto)
                .toList();
    }

    // API 17: Filter components by name
    //@Transactional(readOnly = true)
    public List<PayrollComponentRespDto> getComponentsByName(
            ComponentName name) {

        return componentRepository.findByComponentName(name)
                .stream()
                .map(PayrollComponentMapper::convertEntityToDto)
                .toList();
    }

}
