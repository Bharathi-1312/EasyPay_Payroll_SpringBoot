package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.PayrollComponentDto;
import com.springboot.EasyPay.Dto.response.PayrollComponentRespDto;
import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.mapper.PayrollComponentMapper;
import com.springboot.EasyPay.model.Payroll;
import com.springboot.EasyPay.model.PayrollComponent;
import com.springboot.EasyPay.repository.PayrollRepository;
import com.springboot.EasyPay.repository.PayrollComponentRepository;
import com.springboot.EasyPay.service.PayrollComponentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollComponentServiceTest {

    @InjectMocks
    private PayrollComponentService payrollComponentService;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private PayrollComponentRepository componentRepository;

    // ------------------------------------------------
    // 1. ADD COMPONENT TESTS
    // ------------------------------------------------

    @Test
    void addComponentTest() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        PayrollComponent component = new PayrollComponent();
        PayrollComponent saved = new PayrollComponent();

        PayrollComponentRespDto response =
                mock(PayrollComponentRespDto.class);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        when(dto.amountPercentage())
                .thenReturn(BigDecimal.TEN);

        when(componentRepository.save(any(PayrollComponent.class)))
                .thenReturn(saved);

        try (MockedStatic<PayrollComponentMapper> mapper =
                     mockStatic(PayrollComponentMapper.class)) {

            mapper.when(() ->
                            PayrollComponentMapper.convertDtoToEntity(dto))
                    .thenReturn(component);

            mapper.when(() ->
                            PayrollComponentMapper.convertEntityToDto(saved))
                    .thenReturn(response);

            PayrollComponentRespDto result =
                    payrollComponentService.addComponent(1L, dto);

            assertSame(response, result);
            assertSame(payroll, component.getPayroll());

            verify(componentRepository).save(component);
        }
    }

    @Test
    void addComponentTest_PayrollNotFound() {

        when(payrollRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollComponentService.addComponent(
                        99L, mock(PayrollComponentDto.class)));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    @Test
    void addComponentTest_NotDraft() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.PROCESSED);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        assertThrows(IllegalStateException.class,
                () -> payrollComponentService.addComponent(
                        1L, mock(PayrollComponentDto.class)));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    @Test
    void addComponentTest_NullAmount() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        when(dto.amountPercentage()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> payrollComponentService.addComponent(1L, dto));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    @Test
    void addComponentTest_NegativePercentage() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        when(dto.amountPercentage())
                .thenReturn(new BigDecimal("-5"));

        assertThrows(IllegalArgumentException.class,
                () -> payrollComponentService.addComponent(1L, dto));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    // ------------------------------------------------
    // 2. GET COMPONENTS TESTS
    // ------------------------------------------------

    @Test
    void getComponentsTest() {

        when(payrollRepository.existsById(1L))
                .thenReturn(true);

        PayrollComponent component = new PayrollComponent();

        PayrollComponentRespDto response =
                mock(PayrollComponentRespDto.class);

        when(componentRepository.findByPayrollId(1L))
                .thenReturn(List.of(component));

        try (MockedStatic<PayrollComponentMapper> mapper =
                     mockStatic(PayrollComponentMapper.class)) {

            mapper.when(() ->
                            PayrollComponentMapper.convertEntityToDto(component))
                    .thenReturn(response);

            List<PayrollComponentRespDto> result =
                    payrollComponentService.getComponents(1L);

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getComponentsTest_EmptyList() {

        when(payrollRepository.existsById(1L))
                .thenReturn(true);

        when(componentRepository.findByPayrollId(1L))
                .thenReturn(List.of());

        List<PayrollComponentRespDto> result =
                payrollComponentService.getComponents(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getComponentsTest_PayrollNotFound() {

        when(payrollRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> payrollComponentService.getComponents(99L));

        verify(componentRepository, never()).findByPayrollId(99L);
    }

    // ------------------------------------------------
    // 3. DELETE COMPONENT TESTS
    // ------------------------------------------------

    @Test
    void deleteComponentTest() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponent component = new PayrollComponent();
        component.setPayroll(payroll);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(component));

        assertDoesNotThrow(() ->
                payrollComponentService.deleteComponent(1L));

        verify(componentRepository).delete(component);
    }

    @Test
    void deleteComponentTest_NotFound() {

        when(componentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollComponentService.deleteComponent(99L));

        verify(componentRepository, never())
                .delete(any(PayrollComponent.class));
    }

    @Test
    void deleteComponentTest_NotDraft() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.PAID);

        PayrollComponent component = new PayrollComponent();
        component.setPayroll(payroll);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(component));

        assertThrows(IllegalStateException.class,
                () -> payrollComponentService.deleteComponent(1L));

        verify(componentRepository, never())
                .delete(any(PayrollComponent.class));
    }

    // ------------------------------------------------
    // 4. UPDATE COMPONENT TESTS
    // ------------------------------------------------

    @Test
    void updateComponentTest() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponent component = new PayrollComponent();
        component.setPayroll(payroll);

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        PayrollComponentRespDto response =
                mock(PayrollComponentRespDto.class);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(component));

        when(dto.amountPercentage())
                .thenReturn(BigDecimal.valueOf(25));

        when(dto.componentName())
                .thenReturn(ComponentName.Basic_Pay);

        when(dto.componentType())
                .thenReturn(ComponentType.EARNING);

        when(dto.description())
                .thenReturn("Basic salary");

        when(componentRepository.save(component))
                .thenReturn(component);

        try (MockedStatic<PayrollComponentMapper> mapper =
                     mockStatic(PayrollComponentMapper.class)) {

            mapper.when(() ->
                            PayrollComponentMapper.convertEntityToDto(component))
                    .thenReturn(response);

            PayrollComponentRespDto result =
                    payrollComponentService.updateComponent(1L, dto);

            assertSame(response, result);

            assertEquals(ComponentName.Basic_Pay,
                    component.getComponentName());

            assertEquals(ComponentType.EARNING,
                    component.getComponentType());

            assertEquals(BigDecimal.valueOf(25),
                    component.getAmountPercentage());

            assertEquals("Basic salary",
                    component.getDescription());

            verify(componentRepository).save(component);
        }
    }

    @Test
    void updateComponentTest_NotFound() {

        when(componentRepository.findById(99L))
                .thenReturn(Optional.empty());

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        assertThrows(NoSuchElementException.class,
                () -> payrollComponentService.updateComponent(99L, dto));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    @Test
    void updateComponentTest_NotDraft() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.PROCESSED);

        PayrollComponent component = new PayrollComponent();
        component.setPayroll(payroll);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(component));

        assertThrows(IllegalStateException.class,
                () -> payrollComponentService.updateComponent(
                        1L, mock(PayrollComponentDto.class)));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    @Test
    void updateComponentTest_NullAmount() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponent component = new PayrollComponent();
        component.setPayroll(payroll);

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(component));

        when(dto.amountPercentage()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> payrollComponentService.updateComponent(1L, dto));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    @Test
    void updateComponentTest_NegativeAmount() {

        Payroll payroll = new Payroll();
        payroll.setStatus(PayrollStatus.DRAFT);

        PayrollComponent component = new PayrollComponent();
        component.setPayroll(payroll);

        PayrollComponentDto dto = mock(PayrollComponentDto.class);

        when(componentRepository.findById(1L))
                .thenReturn(Optional.of(component));

        when(dto.amountPercentage())
                .thenReturn(BigDecimal.valueOf(-10));

        assertThrows(IllegalArgumentException.class,
                () -> payrollComponentService.updateComponent(1L, dto));

        verify(componentRepository, never())
                .save(any(PayrollComponent.class));
    }

    // ------------------------------------------------
    // 5. GET COMPONENTS BY TYPE TESTS
    // ------------------------------------------------

    @Test
    void getComponentsByTypeTest() {

        PayrollComponent component = new PayrollComponent();

        PayrollComponentRespDto response =
                mock(PayrollComponentRespDto.class);

        when(componentRepository.findByComponentType(ComponentType.EARNING))
                .thenReturn(List.of(component));

        try (MockedStatic<PayrollComponentMapper> mapper =
                     mockStatic(PayrollComponentMapper.class)) {

            mapper.when(() ->
                            PayrollComponentMapper.convertEntityToDto(component))
                    .thenReturn(response);

            List<PayrollComponentRespDto> result =
                    payrollComponentService.getComponentsByType(
                            ComponentType.EARNING);

            assertEquals(1, result.size());
            assertSame(response, result.get(0));

            verify(componentRepository)
                    .findByComponentType(ComponentType.EARNING);
        }
    }

    @Test
    void getComponentsByTypeTest_EmptyList() {

        when(componentRepository.findByComponentType(ComponentType.EARNING))
                .thenReturn(List.of());

        List<PayrollComponentRespDto> result =
                payrollComponentService.getComponentsByType(
                        ComponentType.EARNING);

        assertTrue(result.isEmpty());
    }

    // ------------------------------------------------
    // 6. GET COMPONENTS BY NAME TESTS
    // ------------------------------------------------

    @Test
    void getComponentsByNameTest() {

        PayrollComponent component = new PayrollComponent();

        PayrollComponentRespDto response =
                mock(PayrollComponentRespDto.class);

        when(componentRepository.findByComponentName(ComponentName.Basic_Pay))
                .thenReturn(List.of(component));

        try (MockedStatic<PayrollComponentMapper> mapper =
                     mockStatic(PayrollComponentMapper.class)) {

            mapper.when(() ->
                            PayrollComponentMapper.convertEntityToDto(component))
                    .thenReturn(response);

            List<PayrollComponentRespDto> result =
                    payrollComponentService.getComponentsByName(
                            ComponentName.Basic_Pay);

            assertEquals(1, result.size());
            assertSame(response, result.get(0));

            verify(componentRepository)
                    .findByComponentName(ComponentName.Basic_Pay);
        }
    }

    @Test
    void getComponentsByNameTest_EmptyList() {

        when(componentRepository.findByComponentName(ComponentName.Basic_Pay))
                .thenReturn(List.of());

        List<PayrollComponentRespDto> result =
                payrollComponentService.getComponentsByName(
                        ComponentName.Basic_Pay);

        assertTrue(result.isEmpty());
    }
}