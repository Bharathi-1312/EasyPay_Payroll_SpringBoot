package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.request.PayrollDto;
import com.springboot.EasyPay.Dto.response.*;
import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.service.PayrollService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
//@RequestMapping("/api/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/api/payroll/{employeeId}")
    //@ResponseStatus(HttpStatus.CREATED)
    //@PreAuthorize("hasRole('HR')")
    public PayrollRespDto createPayroll(@PathVariable Long employeeId
            ,@RequestBody PayrollDto dto, Principal principal) {
        return payrollService.createPayroll(employeeId,dto,principal.getName());
    }

    @PostMapping("/api/payroll/process/{payrollId}")
    // @PreAuthorize("hasRole('HR')")
    public PayrollRespDto processPayroll(
            @PathVariable Long payrollId,
            Principal principal) {
        return payrollService.processPayroll(payrollId,principal.getName());
    }

    @PutMapping("/api/payroll/pay/{payrollId}")
    //@PreAuthorize("hasRole('HR')")
    public PayrollRespDto markAsPaid(@PathVariable Long payrollId,Principal principal) {
        return payrollService.markAsPaid(payrollId,principal.getName());
    }

    @GetMapping("/api/payroll/payslip/{payrollId}")
    //@PreAuthorize("hasAnyRole('HR', 'EMPLOYEE')")
    public PayrollRespDto getPayslip(@PathVariable Long payrollId,Principal principal) {
        return payrollService.getPayslip(payrollId,principal.getName());
    }

    @GetMapping("/api/payroll/employee/{employeeId}")
    //@PreAuthorize("hasAnyRole('HR', 'EMPLOYEE')")
    public List<PayrollRespDto> getEmployeePayslips(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid page or size");
        }

        Pageable pageable = PageRequest.of(page, size);

        return payrollService.getEmployeePayslips(
                employeeId, pageable,principal.getName());
    }

    @GetMapping("/api/payroll/status/{status}")
    //@PreAuthorize("hasRole('HR')")
    public List<PayrollRespDto> byStatus(@PathVariable PayrollStatus status,
                                         Principal principal) {
        return payrollService.getByStatus(status,principal.getName());
    }

    // 3. Payroll by date range
    @GetMapping("/api/payroll/period")
    //@PreAuthorize("hasRole('HR')")
    public List<PayrollRespDto> byPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,Principal principal
            ) {
        return payrollService.getByPeriod(start, end,principal.getName());
    }

    // 4. Search by employee and date range
    @GetMapping("api/payroll/search")
    //@PreAuthorize("hasRole('HR')")
    public List<PayrollRespDto> search(
            @RequestParam Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end,
            Principal principal) {
        return payrollService.searchPayroll(employeeId, start, end,principal.getName());
    }

    // 7. Earnings history
    @GetMapping("/api/employee/earnings/{id}")
    public List<PayrollEarningsDto> earnings(@PathVariable Long id,Principal principal) {
        return payrollService.getEarnings(id,principal.getName());
    }

    // 8. Deductions history
    @GetMapping("/api/employee/deductions/{id}")
    public List<PayrollDeductionDto> deductions(@PathVariable Long id,Principal principal) {
        return payrollService.getDeductions(id,principal.getName());
    }

    // 12. Monthly report
    @GetMapping("/api/payroll/reports/monthly")
    public MonthlyPaymentDto monthly(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {
        return payrollService.monthlyReport(year, month,principal.getName());
    }

    // 18. Printable payslip text
    @GetMapping("/api/payroll/payslip-text/{payrollId}")
    public String payslipText(@PathVariable Long payrollId,Principal principal) {
        return payrollService.getPayslipText(payrollId,principal.getName());
    }

    // 20. Payment status
    @GetMapping("/api/payroll/payment-status/{payrollId}")
    //@PreAuthorize("hasRole('HR')")
    public PaymentStatusDto paymentStatus(@PathVariable Long payrollId,Principal principal) {
        return payrollService.getPaymentStatus(payrollId,principal.getName());
    }

    @GetMapping("/api/payroll/my-payroll")
    //@PreAuthorize("hasAnyRole('EMPLOYEE', 'HR', 'MANAGER')")
    public List<PayrollRespDto> getMyPayroll(
            @AuthenticationPrincipal UserDetails userDetails) {

        return payrollService.getMyPayroll(
                userDetails.getUsername());
    }
}
