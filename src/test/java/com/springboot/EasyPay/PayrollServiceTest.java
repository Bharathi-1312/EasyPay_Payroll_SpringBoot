package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.PayrollDto;
import com.springboot.EasyPay.Dto.response.*;
        import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.mapper.PayrollMapper;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Payroll;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.HRRepository;
import com.springboot.EasyPay.repository.PayrollComponentRepository;
import com.springboot.EasyPay.repository.PayrollRepository;
import com.springboot.EasyPay.service.PayrollProcessorService;
import com.springboot.EasyPay.service.PayrollService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @InjectMocks
    private PayrollService payrollService;

    @Mock
    private PayrollProcessorService payrollProcessorService;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private PayrollComponentRepository componentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HRRepository hrRepository;

    private PayrollDto validDto() {
        PayrollDto dto = mock(PayrollDto.class);

        // Use lenient() so Mockito doesn't throw UnnecessaryStubbingException
        // if a test throws an exception before reaching these methods.
        lenient().when(dto.payPeriodStart())
                .thenReturn(LocalDate.of(2026, 10, 1));

        lenient().when(dto.payPeriodEnd())
                .thenReturn(LocalDate.of(2026, 10, 31));

        return dto;
    }

    private HR createHr(Long id) {
        HR hr = new HR();
        hr.setId(id);
        return hr;
    }

    private Employee createEmployee(Long id, HR hr) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setHr(hr);
        employee.setAnnualPackage(new BigDecimal("600000"));
        return employee;
    }

    private Payroll createPayroll(PayrollStatus status, HR hr) {
        Payroll payroll = new Payroll();

        Employee employee = createEmployee(1L, hr);

        payroll.setEmployee(employee);
        payroll.setHr(hr);
        payroll.setStatus(status);
        payroll.setBasicSalary(new BigDecimal("50000"));
        payroll.setTotalEarnings(BigDecimal.ZERO);
        payroll.setTotalDeductions(BigDecimal.ZERO);
        payroll.setNetSalary(BigDecimal.ZERO);
        payroll.setLeaveDeduction(BigDecimal.ZERO);
        payroll.setPaidLeaveDays(0);
        payroll.setUnpaidLeaveDays(0);
        payroll.setPayPeriodStart(LocalDate.of(2026, 10, 1));
        payroll.setPayPeriodEnd(LocalDate.of(2026, 10, 31));

        return payroll;
    }

    // ==================================================
    // 1. CREATE PAYROLL
    // ==================================================

    @Test
    void createPayrollTest() {

        HR hr = createHr(10L);
        Employee employee = createEmployee(1L, hr);

        PayrollDto dto = validDto();

        PayrollRespDto response = mock(PayrollRespDto.class);
        Payroll savedPayroll = new Payroll();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository
                .existsByEmployee_IdAndPayPeriodStartAndPayPeriodEnd(
                        1L,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)))
                .thenReturn(false);

        when(payrollRepository.save(any(Payroll.class)))
                .thenReturn(savedPayroll);

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(savedPayroll))
                    .thenReturn(response);

            PayrollRespDto result =
                    payrollService.createPayroll(1L, dto, "hr1");

            assertSame(response, result);

            verify(payrollRepository).save(
                    argThat(payroll ->
                            payroll.getEmployee() == employee
                                    && payroll.getHr() == hr
                                    && payroll.getStatus() == PayrollStatus.DRAFT
                                    && payroll.getBasicSalary()
                                    .compareTo(new BigDecimal("50000.00")) == 0
                                    && payroll.getTotalEarnings()
                                    .compareTo(BigDecimal.ZERO) == 0
                                    && payroll.getTotalDeductions()
                                    .compareTo(BigDecimal.ZERO) == 0
                                    && payroll.getNetSalary()
                                    .compareTo(BigDecimal.ZERO) == 0
                    ));
        }
    }

    @Test
    void createPayrollTest_EmployeeNotFound() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.createPayroll(
                        99L, validDto(), "hr1"));

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    @Test
    void createPayrollTest_NullStartDate() {
        // Need to provide an employee because the service checks this BEFORE the dates
        Employee employee = createEmployee(1L, createHr(10L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        PayrollDto dto = mock(PayrollDto.class);
        when(dto.payPeriodStart()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.createPayroll(1L, dto, "hr1"));
    }

    @Test
    void createPayrollTest_NullEndDate() {
        // Need to provide an employee because the service checks this BEFORE the dates
        Employee employee = createEmployee(1L, createHr(10L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        PayrollDto dto = mock(PayrollDto.class);

        lenient().when(dto.payPeriodStart())
                .thenReturn(LocalDate.of(2026, 10, 1));

        when(dto.payPeriodEnd()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.createPayroll(1L, dto, "hr1"));
    }

    @Test
    void createPayrollTest_InvalidPeriod() {
        // Need to provide an employee because the service checks this BEFORE the dates
        Employee employee = createEmployee(1L, createHr(10L));
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        PayrollDto dto = mock(PayrollDto.class);

        lenient().when(dto.payPeriodStart())
                .thenReturn(LocalDate.of(2026, 10, 31));

        lenient().when(dto.payPeriodEnd())
                .thenReturn(LocalDate.of(2026, 10, 1));

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.createPayroll(1L, dto, "hr1"));
    }

    @Test
    void createPayrollTest_IncompleteCalendarMonth() {

        HR hr = createHr(10L);
        Employee employee = createEmployee(1L, hr);

        PayrollDto dto = mock(PayrollDto.class);

        when(dto.payPeriodStart())
                .thenReturn(LocalDate.of(2026, 10, 2));

        when(dto.payPeriodEnd())
                .thenReturn(LocalDate.of(2026, 10, 31));

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.createPayroll(1L, dto, "hr1"));

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    @Test
    void createPayrollTest_HrNotFound() {

        Employee employee = createEmployee(1L, createHr(10L));

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> payrollService.createPayroll(
                        1L, validDto(), "hr1"));

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    @Test
    void createPayrollTest_UnauthorizedHr() {

        Employee employee = createEmployee(1L, createHr(20L));

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        assertThrows(RuntimeException.class,
                () -> payrollService.createPayroll(
                        1L, validDto(), "hr1"));

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    @Test
    void createPayrollTest_MissingAnnualPackage() {

        HR hr = createHr(10L);
        Employee employee = createEmployee(1L, hr);
        employee.setAnnualPackage(null);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        assertThrows(IllegalStateException.class,
                () -> payrollService.createPayroll(
                        1L, validDto(), "hr1"));
    }

    @Test
    void createPayrollTest_ZeroAnnualPackage() {

        HR hr = createHr(10L);
        Employee employee = createEmployee(1L, hr);
        employee.setAnnualPackage(BigDecimal.ZERO);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        assertThrows(IllegalStateException.class,
                () -> payrollService.createPayroll(
                        1L, validDto(), "hr1"));
    }

    @Test
    void createPayrollTest_DuplicatePayroll() {

        HR hr = createHr(10L);
        Employee employee = createEmployee(1L, hr);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository
                .existsByEmployee_IdAndPayPeriodStartAndPayPeriodEnd(
                        1L,
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> payrollService.createPayroll(
                        1L, validDto(), "hr1"));

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    // ==================================================
    // 2. PROCESS PAYROLL
    // ==================================================

    @Test
    void processPayrollTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.DRAFT, hr);

        PayrollRespDto response = mock(PayrollRespDto.class);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        when(payrollRepository.save(payroll))
                .thenReturn(payroll);

        // FIXED: Using thenReturn instead of doNothing because calculatePayroll returns a Payroll object
        when(payrollProcessorService.calculatePayroll(payroll))
                .thenReturn(payroll);

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            PayrollRespDto result =
                    payrollService.processPayroll(1L, "hr1");

            assertSame(response, result);
            assertEquals(PayrollStatus.PROCESSED,
                    payroll.getStatus());
            assertNotNull(payroll.getProcessedAt());
            assertSame(hr, payroll.getHr());

            verify(payrollProcessorService)
                    .calculatePayroll(payroll);

            verify(payrollRepository).save(payroll);
        }
    }

    @Test
    void processPayrollTest_HrNotFound() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> payrollService.processPayroll(1L, "hr1"));

        verify(payrollRepository, never()).findById(anyLong());
    }

    @Test
    void processPayrollTest_NotFound() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.processPayroll(99L, "hr1"));

        verify(payrollProcessorService, never())
                .calculatePayroll(any());
    }

    @Test
    void processPayrollTest_NotDraft() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PROCESSED, hr);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        assertThrows(IllegalStateException.class,
                () -> payrollService.processPayroll(1L, "hr1"));

        verify(payrollProcessorService, never())
                .calculatePayroll(any());

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    @Test
    void processPayrollTest_UnauthorizedHr() {

        HR loggedInHr = createHr(10L);
        HR assignedHr = createHr(20L);

        Payroll payroll = createPayroll(
                PayrollStatus.DRAFT, assignedHr);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(loggedInHr);

        when(payrollRepository.findById(1L))
                .thenReturn(Optional.of(payroll));

        assertThrows(RuntimeException.class,
                () -> payrollService.processPayroll(1L, "hr1"));

        verify(payrollProcessorService, never())
                .calculatePayroll(any());

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    // ==================================================
    // 3. MARK PAYROLL AS PAID
    // ==================================================

    @Test
    void markAsPaidTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PROCESSED, hr);

        PayrollRespDto response = mock(PayrollRespDto.class);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findAuthorizedPayroll(1L, 10L))
                .thenReturn(Optional.of(payroll));

        when(payrollRepository.save(payroll))
                .thenReturn(payroll);

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            PayrollRespDto result =
                    payrollService.markAsPaid(1L, "hr1");

            assertSame(response, result);
            assertEquals(PayrollStatus.PAID, payroll.getStatus());

            verify(payrollRepository).save(payroll);
        }
    }

    @Test
    void markAsPaidTest_NotProcessed() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.DRAFT, hr);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findAuthorizedPayroll(1L, 10L))
                .thenReturn(Optional.of(payroll));

        assertThrows(IllegalStateException.class,
                () -> payrollService.markAsPaid(1L, "hr1"));

        verify(payrollRepository, never())
                .save(any(Payroll.class));
    }

    @Test
    void markAsPaidTest_AccessDenied() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findAuthorizedPayroll(99L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.markAsPaid(99L, "hr1"));
    }

    // ==================================================
    // 4. GET PAYSLIP
    // ==================================================

    @Test
    void getPayslipTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PAID, hr);

        PayrollRespDto response = mock(PayrollRespDto.class);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findAuthorizedPayroll(1L, 10L))
                .thenReturn(Optional.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            assertSame(response,
                    payrollService.getPayslip(1L, "hr1"));
        }
    }

    @Test
    void getPayslipTest_AccessDenied() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findAuthorizedPayroll(99L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.getPayslip(99L, "hr1"));
    }

    // ==================================================
    // 5. GET EMPLOYEE PAYSLIPS
    // ==================================================

    @Test
    void getEmployeePayslipsTest_EmployeeOwnPayroll() {

        Employee employee = createEmployee(1L, createHr(10L));

        Pageable pageable = PageRequest.of(0, 5);

        List<PayrollRespDto> payslips =
                List.of(mock(PayrollRespDto.class));

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(employee));

        when(payrollRepository.getPayrollByEmployeeId(
                1L, pageable))
                .thenReturn(payslips);

        List<PayrollRespDto> result =
                payrollService.getEmployeePayslips(
                        1L, pageable, "emp1");

        assertEquals(1, result.size());

        verify(payrollRepository)
                .getPayrollByEmployeeId(1L, pageable);
    }

    @Test
    void getEmployeePayslipsTest_EmployeeUnauthorized() {

        Employee employee = createEmployee(1L, createHr(10L));

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(employee));

        assertThrows(RuntimeException.class,
                () -> payrollService.getEmployeePayslips(
                        2L, PageRequest.of(0, 5), "emp1"));

        verify(payrollRepository, never())
                .getPayrollByEmployeeId(anyLong(), any(Pageable.class));
    }

    @Test
    void getEmployeePayslipsTest_HrAccess() {

        HR hr = createHr(10L);

        Pageable pageable = PageRequest.of(0, 5);

        List<PayrollRespDto> payslips =
                List.of(mock(PayrollRespDto.class));

        when(employeeRepository.findEmployeeByUsername("hr1"))
                .thenReturn(Optional.empty());

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.getPayrollByEmployeeIdAndHr(
                1L, 10L, pageable))
                .thenReturn(payslips);

        List<PayrollRespDto> result =
                payrollService.getEmployeePayslips(
                        1L, pageable, "hr1");

        assertEquals(1, result.size());

        verify(payrollRepository)
                .getPayrollByEmployeeIdAndHr(
                        1L, 10L, pageable);
    }

    @Test
    void getEmployeePayslipsTest_HrNotFound() {

        when(employeeRepository.findEmployeeByUsername("hr1"))
                .thenReturn(Optional.empty());

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> payrollService.getEmployeePayslips(
                        1L, PageRequest.of(0, 5), "hr1"));
    }

    // ==================================================
    // 6. GET PROCESSED PAYROLLS BY HR
    // ==================================================

    @Test
    void getProcessedByHrTest() {

        List<Payroll> payrolls = List.of(
                new Payroll(), new Payroll());

        when(payrollRepository.findPayrollsProcessedByHr(10L))
                .thenReturn(payrolls);

        List<Payroll> result =
                payrollService.getProcessedByHr(10L);

        assertEquals(2, result.size());

        verify(payrollRepository)
                .findPayrollsProcessedByHr(10L);
    }

    @Test
    void getProcessedByHrTest_EmptyList() {

        when(payrollRepository.findPayrollsProcessedByHr(10L))
                .thenReturn(List.of());

        assertTrue(payrollService.getProcessedByHr(10L).isEmpty());
    }

    // ==================================================
    // 7. FILTER PAYROLL BY STATUS
    // ==================================================

    @Test
    void getByStatusTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PROCESSED, hr);

        PayrollRespDto response = mock(PayrollRespDto.class);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findByStatusAndHr(
                PayrollStatus.PROCESSED, 10L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            List<PayrollRespDto> result =
                    payrollService.getByStatus(
                            PayrollStatus.PROCESSED, "hr1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getByStatusTest_EmptyList() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findByStatusAndHr(
                PayrollStatus.DRAFT, 10L))
                .thenReturn(List.of());

        assertTrue(payrollService.getByStatus(
                PayrollStatus.DRAFT, "hr1").isEmpty());
    }

    // ==================================================
    // 8. FILTER PAYROLL BY PERIOD
    // ==================================================

    @Test
    void getByPeriodTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PROCESSED, hr);

        PayrollRespDto response = mock(PayrollRespDto.class);

        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findByPeriodAndHr(
                start, end, 10L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            List<PayrollRespDto> result =
                    payrollService.getByPeriod(start, end, "hr1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getByPeriodTest_InvalidDates() {

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.getByPeriod(
                        LocalDate.of(2026, 10, 31),
                        LocalDate.of(2026, 10, 1),
                        "hr1"));

        verify(hrRepository, never()).getByUsername(anyString());
    }

    @Test
    void getByPeriodTest_NullStartDate() {

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.getByPeriod(
                        null,
                        LocalDate.of(2026, 10, 31),
                        "hr1"));
    }

    @Test
    void getByPeriodTest_EmptyList() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findByPeriodAndHr(
                any(LocalDate.class),
                any(LocalDate.class),
                eq(10L)))
                .thenReturn(List.of());

        assertTrue(payrollService.getByPeriod(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                "hr1").isEmpty());
    }

    // ==================================================
    // 9. SEARCH PAYROLL
    // ==================================================

    @Test
    void searchPayrollTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PROCESSED, hr);

        PayrollRespDto response = mock(PayrollRespDto.class);

        LocalDate start = LocalDate.of(2026, 10, 1);
        LocalDate end = LocalDate.of(2026, 10, 31);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.searchPayrollForHr(
                1L, start, end, 10L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            List<PayrollRespDto> result =
                    payrollService.searchPayroll(
                            1L, start, end, "hr1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void searchPayrollTest_InvalidDates() {

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.searchPayroll(
                        1L,
                        LocalDate.of(2026, 10, 31),
                        LocalDate.of(2026, 10, 1),
                        "hr1"));

        verify(hrRepository, never()).getByUsername(anyString());
    }

    @Test
    void searchPayrollTest_EmptyList() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.searchPayrollForHr(
                eq(1L),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(10L)))
                .thenReturn(List.of());

        assertTrue(payrollService.searchPayroll(
                1L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                "hr1").isEmpty());
    }

    // ==================================================
    // 10. EARNINGS HISTORY
    // ==================================================

    @Test
    void getEarningsTest_EmployeeOwnEarnings() {

        Employee employee = createEmployee(1L, createHr(10L));

        Payroll payroll = new Payroll();

        PayrollEarningsDto response =
                mock(PayrollEarningsDto.class);

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(employee));

        when(payrollRepository.findEmployeePayrollHistory(1L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.toEarningsResponse(payroll))
                    .thenReturn(response);

            List<PayrollEarningsDto> result =
                    payrollService.getEarnings(1L, "emp1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getEarningsTest_EmployeeUnauthorized() {

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(
                        createEmployee(1L, createHr(10L))));

        assertThrows(RuntimeException.class,
                () -> payrollService.getEarnings(2L, "emp1"));

        verify(payrollRepository, never())
                .findEmployeePayrollHistory(anyLong());
    }

    @Test
    void getEarningsTest_HrAccess() {

        HR hr = createHr(10L);

        Payroll payroll = new Payroll();

        PayrollEarningsDto response =
                mock(PayrollEarningsDto.class);

        when(employeeRepository.findEmployeeByUsername("hr1"))
                .thenReturn(Optional.empty());

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findEmployeePayrollHistoryForHr(
                1L, 10L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.toEarningsResponse(payroll))
                    .thenReturn(response);

            List<PayrollEarningsDto> result =
                    payrollService.getEarnings(1L, "hr1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getEarningsTest_EmptyList() {

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(
                        createEmployee(1L, createHr(10L))));

        when(payrollRepository.findEmployeePayrollHistory(1L))
                .thenReturn(List.of());

        assertTrue(payrollService.getEarnings(
                1L, "emp1").isEmpty());
    }

    // ==================================================
    // 11. DEDUCTION HISTORY
    // ==================================================

    @Test
    void getDeductionsTest_EmployeeOwnDeductions() {

        Employee employee = createEmployee(1L, createHr(10L));

        Payroll payroll = new Payroll();

        PayrollDeductionDto response =
                mock(PayrollDeductionDto.class);

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(employee));

        when(payrollRepository.findEmployeePayrollHistory(1L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.toDeductionsResponse(payroll))
                    .thenReturn(response);

            List<PayrollDeductionDto> result =
                    payrollService.getDeductions(1L, "emp1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getDeductionsTest_EmployeeUnauthorized() {

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(
                        createEmployee(1L, createHr(10L))));

        assertThrows(RuntimeException.class,
                () -> payrollService.getDeductions(2L, "emp1"));

        verify(payrollRepository, never())
                .findEmployeePayrollHistory(anyLong());
    }

    @Test
    void getDeductionsTest_HrAccess() {

        HR hr = createHr(10L);

        Payroll payroll = new Payroll();

        PayrollDeductionDto response =
                mock(PayrollDeductionDto.class);

        when(employeeRepository.findEmployeeByUsername("hr1"))
                .thenReturn(Optional.empty());

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findEmployeePayrollHistoryForHr(
                1L, 10L))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.toDeductionsResponse(payroll))
                    .thenReturn(response);

            List<PayrollDeductionDto> result =
                    payrollService.getDeductions(1L, "hr1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));
        }
    }

    @Test
    void getDeductionsTest_EmptyList() {

        when(employeeRepository.findEmployeeByUsername("emp1"))
                .thenReturn(Optional.of(
                        createEmployee(1L, createHr(10L))));

        when(payrollRepository.findEmployeePayrollHistory(1L))
                .thenReturn(List.of());

        assertTrue(payrollService.getDeductions(
                1L, "emp1").isEmpty());
    }

    // ==================================================
    // 12. MONTHLY PAYMENT REPORT
    // ==================================================

    @Test
    void monthlyReportTest() {

        HR hr = createHr(10L);

        Payroll payroll1 = new Payroll();
        payroll1.setTotalEarnings(new BigDecimal("50000"));
        payroll1.setTotalDeductions(new BigDecimal("5000"));
        payroll1.setNetSalary(new BigDecimal("45000"));

        Payroll payroll2 = new Payroll();
        payroll2.setTotalEarnings(new BigDecimal("60000"));
        payroll2.setTotalDeductions(new BigDecimal("6000"));
        payroll2.setNetSalary(new BigDecimal("54000"));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findPayrollsOverlappingPeriodForHr(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                10L))
                .thenReturn(List.of(payroll1, payroll2));

        MonthlyPaymentDto result =
                payrollService.monthlyReport(2026, 10, "hr1");

        assertNotNull(result);
        assertEquals(2026, result.year());
        assertEquals(10, result.month());

        // FIXED: Intentionally removed the erroneous assertEquals(2, result.totalNetSalary()); which crashes runtime tests due to type mismatch

        assertEquals(new BigDecimal("110000"),
                result.totalEarnings());

        assertEquals(new BigDecimal("11000"),
                result.totalDeductions());

        assertEquals(new BigDecimal("99000"),
                result.totalNetSalary());
    }

    @Test
    void monthlyReportTest_InvalidMonth() {

        assertThrows(IllegalArgumentException.class,
                () -> payrollService.monthlyReport(
                        2026, 13, "hr1"));

        verify(hrRepository, never()).getByUsername(anyString());
    }

    @Test
    void monthlyReportTest_EmptyList() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findPayrollsOverlappingPeriodForHr(
                any(LocalDate.class),
                any(LocalDate.class),
                eq(10L)))
                .thenReturn(List.of());

        MonthlyPaymentDto result =
                payrollService.monthlyReport(2026, 10, "hr1");

        // FIXED: Removed assertEquals(0, result.totalNetSalary()) to solve int vs BigDecimal type exception crash. Replaced with proper BigDecimal.ZERO checks.
        assertEquals(BigDecimal.ZERO, result.totalEarnings());
        assertEquals(BigDecimal.ZERO, result.totalDeductions());
        assertEquals(BigDecimal.ZERO, result.totalNetSalary());
    }

    // ==================================================
    // 13. GET PAYSLIP TEXT
    // ==================================================

    @Test
    void getPayslipTextTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PAID, hr);

        payroll.setId(1L);

        payroll.getEmployee().setFullName("Test Employee");

        payroll.getEmployee().setAnnualPackage(
                new BigDecimal("600000"));

        payroll.setTotalEarnings(new BigDecimal("55000"));
        payroll.setTotalDeductions(new BigDecimal("5000"));
        payroll.setNetSalary(new BigDecimal("50000"));

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findAuthorizedPayroll(1L, 10L))
                .thenReturn(Optional.of(payroll));

        String result =
                payrollService.getPayslipText(1L, "hr1");

        assertNotNull(result);
        assertTrue(result.contains("EASYPAY PAYSLIP"));
        assertTrue(result.contains("Payroll ID: 1"));
        assertTrue(result.contains("Employee Name: Test Employee"));
        assertTrue(result.contains("Annual Package: 600000"));
        assertTrue(result.contains("Monthly Basic Salary: 50000"));
        assertTrue(result.contains("Total Earnings: 55000"));
        assertTrue(result.contains("Total Deductions: 5000"));
        assertTrue(result.contains("Net Salary: 50000"));
        assertTrue(result.contains("Status: PAID"));
    }

    @Test
    void getPayslipTextTest_AccessDenied() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findAuthorizedPayroll(99L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.getPayslipText(99L, "hr1"));
    }

    // ==================================================
    // 14. GET PAYMENT STATUS
    // ==================================================

    @Test
    void getPaymentStatusTest() {

        HR hr = createHr(10L);

        Payroll payroll = createPayroll(
                PayrollStatus.PAID, hr);

        PaymentStatusDto response =
                mock(PaymentStatusDto.class);

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(hr);

        when(payrollRepository.findAuthorizedPayroll(1L, 10L))
                .thenReturn(Optional.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.toPaymentStatusResponse(payroll))
                    .thenReturn(response);

            PaymentStatusDto result =
                    payrollService.getPaymentStatus(1L, "hr1");

            assertSame(response, result);
        }
    }

    @Test
    void getPaymentStatusTest_AccessDenied() {

        when(hrRepository.getByUsername("hr1"))
                .thenReturn(createHr(10L));

        when(payrollRepository.findAuthorizedPayroll(99L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> payrollService.getPaymentStatus(99L, "hr1"));
    }

    // ==================================================
    // 15. GET MY PAYROLL
    // ==================================================

    @Test
    void getMyPayrollTest() {

        Payroll payroll = new Payroll();

        PayrollRespDto response =
                mock(PayrollRespDto.class);

        when(payrollRepository.findMyPayroll("emp1"))
                .thenReturn(List.of(payroll));

        try (MockedStatic<PayrollMapper> mapper =
                     mockStatic(PayrollMapper.class)) {

            mapper.when(() ->
                            PayrollMapper.convertEntityToDto(payroll))
                    .thenReturn(response);

            List<PayrollRespDto> result =
                    payrollService.getMyPayroll("emp1");

            assertEquals(1, result.size());
            assertSame(response, result.get(0));

            verify(payrollRepository).findMyPayroll("emp1");
        }
    }

    @Test
    void getMyPayrollTest_EmptyList() {

        when(payrollRepository.findMyPayroll("emp1"))
                .thenReturn(List.of());

        assertTrue(payrollService.getMyPayroll("emp1").isEmpty());
    }
}

