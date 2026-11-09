package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.PayrollComponentDto;
import com.springboot.EasyPay.Dto.request.PayrollDto;
import com.springboot.EasyPay.Dto.response.*;
import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.mapper.PayrollComponentMapper;
import com.springboot.EasyPay.mapper.PayrollMapper;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Payroll;
import com.springboot.EasyPay.model.PayrollComponent;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.HRRepository;
import com.springboot.EasyPay.repository.PayrollComponentRepository;
import com.springboot.EasyPay.repository.PayrollRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor

public class PayrollService {

    private final PayrollProcessorService payrollProcessorService;
    private final PayrollRepository payrollRepository;
    private final PayrollComponentRepository componentRepository;
    private final EmployeeRepository employeeRepository;
    private final HRRepository hrRepository;


    private Employee getLoggedInEmployee(String username) {

        return employeeRepository.findEmployeeByUsername(username)
                .orElse(null);
    }
    private HR getLoggedInHr(String username) {
        HR hr = hrRepository.getByUsername(username);

        if (hr == null) {
            throw new RuntimeException("HR not found");
        }

        return hr;
    }

    private Payroll getAuthorizedPayroll(
            Long payrollId, String username) {

        HR hr = getLoggedInHr(username);

        return payrollRepository.findAuthorizedPayroll(
                        payrollId, hr.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payroll not found or access denied"));
    }
    //@Transactional
    public PayrollRespDto createPayroll(Long employeeId,PayrollDto dto,String username) {


        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        if (dto.payPeriodStart() == null ||
                dto.payPeriodEnd() == null ||
                dto.payPeriodEnd().isBefore(dto.payPeriodStart())) {

            throw new IllegalArgumentException("Invalid pay period");
        }
        HR hr = getLoggedInHr(username);

        if (employee.getHr() == null ||
                !employee.getHr().getId().equals(hr.getId())) {

            throw new RuntimeException(
                    "You are not authorized to create payroll for this employee");
        }

        if (!dto.payPeriodStart().withDayOfMonth(1)
                .equals(dto.payPeriodStart()) ||
                !dto.payPeriodEnd().equals(
                        dto.payPeriodStart()
                                .withDayOfMonth(1)
                                .plusMonths(1)
                                .minusDays(1))) {

            throw new IllegalArgumentException(
                    "Payroll must cover one complete calendar month");
        }

        if (employee.getAnnualPackage() == null ||
                employee.getAnnualPackage()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                    "Employee annual package is missing");
        }

        boolean exists = payrollRepository.existsByEmployee_IdAndPayPeriodStartAndPayPeriodEnd(
                employeeId,
                dto.payPeriodStart(),
                dto.payPeriodEnd()
        );

        if (exists) {
            throw new IllegalStateException(
                    "Payroll already exists for this employee and month");
        }

        BigDecimal monthlySalary =
                employee.getAnnualPackage().divide(
                        BigDecimal.valueOf(12),
                        2,
                        RoundingMode.HALF_UP
                );

        Payroll payroll = new Payroll();

        payroll.setEmployee(employee);
        payroll.setEmployee(employee);
        payroll.setHr(hr); // Assign HR even when payroll is in DRAFT
        payroll.setStatus(PayrollStatus.DRAFT);

        payroll.setPayPeriodStart(dto.payPeriodStart());
        payroll.setPayPeriodEnd(dto.payPeriodEnd());

        payroll.setBasicSalary(monthlySalary);
        payroll.setTotalEarnings(BigDecimal.ZERO);
        payroll.setTotalDeductions(BigDecimal.ZERO);
        payroll.setNetSalary(BigDecimal.ZERO);
        payroll.setLeaveDeduction(BigDecimal.ZERO);

        payroll.setPaidLeaveDays(0);
        payroll.setUnpaidLeaveDays(0);

        return PayrollMapper.convertEntityToDto(
                payrollRepository.save(payroll)
        );
    }

    //@Transactional
    public PayrollRespDto processPayroll(Long payrollId,String hrUsername) {

        HR hr = getLoggedInHr(hrUsername);

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() ->
                        new RuntimeException("Payroll not found"));

        if (payroll.getEmployee().getHr() == null ||
                !payroll.getEmployee().getHr().getId()
                        .equals(hr.getId())) {

            throw new RuntimeException(
                    "You are not authorized to process this employee's payroll");
        }

        if (payroll.getStatus() != PayrollStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft payroll can be processed");
        }

        // All calculations are delegated to PayrollProcessorService.

        payrollProcessorService.calculatePayroll(payroll);

        payroll.setStatus(PayrollStatus.PROCESSED);

        payroll.setProcessedAt(LocalDateTime.now());

        payroll.setHr(hr);

        Payroll savedPayroll = payrollRepository.save(payroll);

        return PayrollMapper.convertEntityToDto(savedPayroll);
    }

    public PayrollRespDto markAsPaid(
            Long payrollId, String username) {

        Payroll payroll = getAuthorizedPayroll(
                payrollId, username);

        if (payroll.getStatus() != PayrollStatus.PROCESSED) {
            throw new IllegalStateException(
                    "Only processed payroll can be marked as paid");
        }

        payroll.setStatus(PayrollStatus.PAID);

        return PayrollMapper.convertEntityToDto(
                payrollRepository.save(payroll));
    }

    //@Transactional(readOnly = true)
    public PayrollRespDto getPayslip(Long payrollId,String username) {

        Payroll payroll = getAuthorizedPayroll(
                payrollId, username);

        return PayrollMapper.convertEntityToDto(payroll);
    }

    //@Transactional(readOnly = true)
    public List<PayrollRespDto> getEmployeePayslips(
            Long employeeId,
            Pageable pageable,String username) {

        // Check whether the logged-in user is an employee.
        Employee employee = getLoggedInEmployee(username);

        if (employee != null) {

            // Employee can view only their own payroll.
            if (!employee.getId().equals(employeeId)) {
                throw new RuntimeException(
                        "You are not authorized to view this employee's payroll");
            }

            return payrollRepository.getPayrollByEmployeeId(
                    employeeId, pageable);
        }

        // Otherwise, check whether the logged-in user is HR.
        HR hr = getLoggedInHr(username);

        // HR can view only payroll of employees they onboarded
        // and processed.
        return payrollRepository.getPayrollByEmployeeIdAndHr(
                employeeId, hr.getId(), pageable); }





    // API 14: Payroll processed by a particular HR
    //@Transactional(readOnly = true)
    public List<Payroll> getProcessedByHr(Long hrId) {
        return payrollRepository.findPayrollsProcessedByHr(hrId);
    }
    // API 2: Filter by status
    //@Transactional(readOnly = true)
    public List<PayrollRespDto> getByStatus(PayrollStatus status,String username) {
        HR hr = getLoggedInHr(username);

        List<Payroll> payrolls =
                payrollRepository.findByStatusAndHr(
                        status, hr.getId());

        List<PayrollRespDto> result = new ArrayList<>();

        for (Payroll payroll : payrolls) {
            result.add(PayrollMapper.convertEntityToDto(payroll));
        }

        return result;
    }

    // API 3: Payrolls within a date range
    //@Transactional(readOnly = true)
    public List<PayrollRespDto> getByPeriod(
            LocalDate start,
            LocalDate end,
            String username) {

        validateDates(start, end);

        HR hr = getLoggedInHr(username);

        List<Payroll> payrolls =
                payrollRepository.findByPeriodAndHr(
                        start, end, hr.getId());

        List<PayrollRespDto> result = new ArrayList<>();

        for (Payroll payroll : payrolls) {
            result.add(PayrollMapper.convertEntityToDto(payroll));
        }

        return result;
    }
    // API 4: Search by employee and date range
    //@Transactional(readOnly = true)
    public List<PayrollRespDto> searchPayroll(
            Long employeeId,
            LocalDate start,
            LocalDate end,
            String username) {

        validateDates(start, end);

        HR hr = getLoggedInHr(username);

        List<Payroll> payrolls =
                payrollRepository.searchPayrollForHr(
                        employeeId, start, end, hr.getId());

        List<PayrollRespDto> result = new ArrayList<>();

        for (Payroll payroll : payrolls) {
            result.add(PayrollMapper.convertEntityToDto(payroll));
        }

        return result;
    }

    // API 7: Earnings history
    //@Transactional(readOnly = true)
    public List<PayrollEarningsDto> getEarnings(
            Long employeeId, String username) {

        List<Payroll> payrolls;

        // Identify logged-in employee.
        Employee employee = getLoggedInEmployee(username);

        if (employee != null) {

            // Employee can access only their own earnings.
            if (!employee.getId().equals(employeeId)) {
                throw new RuntimeException(
                        "You are not authorized to view these earnings");
            }

            payrolls = payrollRepository
                    .findEmployeePayrollHistory(employeeId);

        } else {

            // HR can access only their assigned employees' earnings.
            HR hr = getLoggedInHr(username);

            payrolls = payrollRepository
                    .findEmployeePayrollHistoryForHr(
                            employeeId, hr.getId());
        }

        List<PayrollEarningsDto> result = new ArrayList<>();

        for (Payroll payroll : payrolls) {
            result.add(
                    PayrollMapper.toEarningsResponse(payroll));
        }

        return result;
    }
    // API 8: Deduction history
    //@Transactional(readOnly = true)
    public List<PayrollDeductionDto> getDeductions(
            Long employeeId, String username) {

        List<Payroll> payrolls;

        // Identify logged-in employee.
        Employee employee = getLoggedInEmployee(username);

        if (employee != null) {

            // Employee can access only their own deductions.
            if (!employee.getId().equals(employeeId)) {
                throw new RuntimeException(
                        "You are not authorized to view these deductions");
            }

            payrolls = payrollRepository
                    .findEmployeePayrollHistory(employeeId);

        } else {

            // HR can access only their assigned employees' deductions.
            HR hr = getLoggedInHr(username);

            payrolls = payrollRepository
                    .findEmployeePayrollHistoryForHr(
                            employeeId, hr.getId());
        }

        List<PayrollDeductionDto> result = new ArrayList<>();

        for (Payroll payroll : payrolls) {
            result.add(
                    PayrollMapper.toDeductionsResponse(payroll));
        }

        return result;
    }

    //@Transactional(readOnly = true)
    public MonthlyPaymentDto monthlyReport(
            int year, int month, String username) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 1 and 12");
        }

        HR hr = getLoggedInHr(username);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Payroll> payrolls =
                payrollRepository.findPayrollsOverlappingPeriodForHr(
                        start, end, hr.getId());

        BigDecimal totalEarnings = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNetSalary = BigDecimal.ZERO;

        for (Payroll payroll : payrolls) {
            totalEarnings = totalEarnings.add(
                    payroll.getTotalEarnings());

            totalDeductions = totalDeductions.add(
                    payroll.getTotalDeductions());

            totalNetSalary = totalNetSalary.add(
                    payroll.getNetSalary());
        }

        return new MonthlyPaymentDto(
                year,
                month,
                payrolls.size(),
                totalEarnings,
                totalDeductions,
                totalNetSalary
        );
    }
    // API 18: Generate printable payslip text
    //@Transactional(readOnly = true)
    public String getPayslipText(
            Long id, String username) {

        Payroll p = getAuthorizedPayroll(id, username);

        return "========== EASYPAY PAYSLIP ==========\n"
                + "Payroll ID: " + p.getId() + "\n"
                + "Employee ID: " + p.getEmployee().getId() + "\n"
                + "Employee Name: " + p.getEmployee().getFullName() + "\n"
                + "Annual Package: " + p.getEmployee().getAnnualPackage() + "\n"
                + "Pay Period: " + p.getPayPeriodStart()
                + " to " + p.getPayPeriodEnd() + "\n"
                + "\n---------- EARNINGS ----------\n"
                + "Monthly Basic Salary: " + p.getBasicSalary() + "\n"
                + "Total Earnings: " + p.getTotalEarnings() + "\n"
                + "\n---------- LEAVE DETAILS ----------\n"
                + "Paid Leave Days: " + p.getPaidLeaveDays() + "\n"
                + "Unpaid Leave Days: " + p.getUnpaidLeaveDays() + "\n"
                + "Leave Deduction: " + p.getLeaveDeduction() + "\n"
                + "\n---------- DEDUCTIONS ----------\n"
                + "Total Deductions: " + p.getTotalDeductions() + "\n"
                + "\n---------- NET PAY ----------\n"
                + "Net Salary: " + p.getNetSalary() + "\n"
                + "Status: " + p.getStatus() + "\n"
                + "Processed At: " + p.getProcessedAt() + "\n"
                + "====================================";
    }

    public PaymentStatusDto getPaymentStatus(
            Long id, String username) {

        Payroll payroll = getAuthorizedPayroll(id, username);

        return PayrollMapper.toPaymentStatusResponse(payroll);
    }



    private Payroll getPayroll(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Payroll not found"));
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid date range");
        }
    }

    //@Transactional(readOnly = true)
    public List<PayrollRespDto> getMyPayroll(String username) {

        List<Payroll> payrolls =
                payrollRepository.findMyPayroll(username);

        List<PayrollRespDto> result = new ArrayList<>();

        for (Payroll payroll : payrolls) {
            result.add(PayrollMapper.convertEntityToDto(payroll));
        }

        return result;
    }
}
