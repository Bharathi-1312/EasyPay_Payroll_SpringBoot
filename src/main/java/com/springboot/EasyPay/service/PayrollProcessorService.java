package com.springboot.EasyPay.service;

import com.springboot.EasyPay.enums.ComponentType;
import com.springboot.EasyPay.enums.LeaveType;
import com.springboot.EasyPay.enums.Status;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.LeaveRequest;
import com.springboot.EasyPay.model.Payroll;
import com.springboot.EasyPay.model.PayrollComponent;
import com.springboot.EasyPay.repository.LeaveRequestRepository;
import com.springboot.EasyPay.repository.PayrollComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PayrollProcessorService {

    private final LeaveRequestRepository leaveRepository;
    private final PayrollComponentRepository componentRepository;

    private static final int MONTHLY_PAID_LEAVE_LIMIT = 5;

    public Payroll calculatePayroll(Payroll payroll) {

        Employee employee = payroll.getEmployee();

        LocalDate start = payroll.getPayPeriodStart();
        LocalDate end = payroll.getPayPeriodEnd();

        int periodDays = (int) ChronoUnit.DAYS.between(start, end) + 1;

        if (periodDays <= 0) {
            throw new IllegalArgumentException("Invalid payroll period");
        }

        // 1. Calculate monthly gross salary from annual package

        BigDecimal annualPackage = employee.getAnnualPackage();

        if (annualPackage == null ||
                annualPackage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Employee annual package is not configured");
        }

        BigDecimal monthlyGross = annualPackage.divide(
                BigDecimal.valueOf(12),
                2,
                RoundingMode.HALF_UP
        );

        // 2. Calculate joining-date proration

        LocalDate effectiveStart = employee.getJoiningDate().isAfter(start)
                ? employee.getJoiningDate()
                : start;

        long payableCalendarDays = effectiveStart.isAfter(end)
                ? 0
                : ChronoUnit.DAYS.between(effectiveStart, end) + 1;

        BigDecimal dailySalary = monthlyGross.divide(
                BigDecimal.valueOf(periodDays),
                4,
                RoundingMode.HALF_UP
        );

        BigDecimal basicSalary = dailySalary.multiply(
                BigDecimal.valueOf(payableCalendarDays)
        ).setScale(2, RoundingMode.HALF_UP);

        // 3. Retrieve approved leave requests

        List<LeaveRequest> approvedLeaves =
                leaveRepository
                        .findOverlappingLeaves(
                                employee.getId(),
                                Status.APPROVED,
                                end,
                                start
                        );

        // Use sets to avoid counting overlapping leave dates twice.

        Set<LocalDate> regularLeaveDays = new HashSet<>();
        Set<LocalDate> medicalLeaveDays = new HashSet<>();

        for (LeaveRequest leave : approvedLeaves) {

            LocalDate leaveStart = leave.getStartDate().isAfter(start)
                    ? leave.getStartDate()
                    : start;

            LocalDate leaveEnd = leave.getEndDate().isBefore(end)
                    ? leave.getEndDate()
                    : end;

            if (leaveStart.isBefore(effectiveStart)) {
                leaveStart = effectiveStart;
            }

            boolean validMedicalLeave =
                    leave.getLeaveType() == LeaveType.SICK
                            && leave.isMedicalCertificateSubmitted();

            for (LocalDate date = leaveStart;
                 !date.isAfter(leaveEnd);
                 date = date.plusDays(1)) {

                if (validMedicalLeave) {
                    medicalLeaveDays.add(date);
                    regularLeaveDays.remove(date);
                } else if (!medicalLeaveDays.contains(date)) {
                    regularLeaveDays.add(date);
                }
            }
        }

        // 4. Apply five-day paid leave allowance

        int regularLeaves = regularLeaveDays.size();

        int paidRegularLeaves = Math.min(
                regularLeaves,
                MONTHLY_PAID_LEAVE_LIMIT
        );

        int unpaidLeaves = Math.max(
                0,
                regularLeaves - MONTHLY_PAID_LEAVE_LIMIT
        );

        int medicalLeaves = medicalLeaveDays.size();

        int totalPaidLeaves = paidRegularLeaves + medicalLeaves;

        // 5. Calculate unpaid leave deduction

        BigDecimal leaveDeduction = dailySalary.multiply(
                BigDecimal.valueOf(unpaidLeaves)
        ).setScale(2, RoundingMode.HALF_UP);

        // 6. Process existing payroll components

        List<PayrollComponent> components =
                componentRepository.findByPayrollId(payroll.getId());

        BigDecimal earnings = basicSalary;
        BigDecimal componentDeductions = BigDecimal.ZERO;

        BigDecimal hundred = BigDecimal.valueOf(100);

        for (PayrollComponent component : components) {

            BigDecimal percentageAmount =
                    component.getAmountPercentage()
                            .divide(hundred, 6, RoundingMode.HALF_UP);

            BigDecimal amount = basicSalary
                    .multiply(percentageAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            if (component.getComponentType() == ComponentType.EARNING) {

                earnings = earnings.add(amount);

            } else if (component.getComponentType()
                    == ComponentType.DEDUCTION) {

                componentDeductions =
                        componentDeductions.add(amount);
            }
        }

        // 7. Calculate final salary

        BigDecimal totalDeductions =
                componentDeductions.add(leaveDeduction);

        BigDecimal netSalary = earnings.subtract(totalDeductions);

        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Net salary cannot be negative");
        }

        // 8. Store all calculation results in Payroll

        payroll.setBasicSalary(basicSalary);
        payroll.setTotalEarnings(
                earnings.setScale(2, RoundingMode.HALF_UP));

        payroll.setTotalDeductions(
                totalDeductions.setScale(2, RoundingMode.HALF_UP));

        payroll.setNetSalary(
                netSalary.setScale(2, RoundingMode.HALF_UP));

        payroll.setLeaveDeduction(leaveDeduction);
        payroll.setPaidLeaveDays(totalPaidLeaves);
        payroll.setUnpaidLeaveDays(unpaidLeaves);

        return payroll;
    }
}
