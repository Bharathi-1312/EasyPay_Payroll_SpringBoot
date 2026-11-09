package com.springboot.EasyPay.repository;

import com.springboot.EasyPay.Dto.response.PayrollRespDto;
import com.springboot.EasyPay.enums.PayrollStatus;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Payroll;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository
        extends JpaRepository<Payroll, Long> {

    @Query("""
            select p.id,
                e.id,
                p.payPeriodStart,
                p.payPeriodEnd,
                p.basicSalary,
                p.totalEarnings,
                p.totalDeductions,
                p.netSalary,
                p.status,
                p.processedAt,
                p.paidLeaveDays,
                p.unpaidLeaveDays,
                p.leaveDeduction
            from Payroll p
            join p.employee e
            where e.id = ?1
            order by p.payPeriodStart desc
            """)
    List<PayrollRespDto> getPayrollByEmployeeId(
            @Param("employeeId") Long employeeId,
            Pageable pageable
    );

    @Query("""
            select p.id,
                e.id,
                p.payPeriodStart,
                p.payPeriodEnd,
                p.basicSalary,
                p.totalEarnings,
                p.totalDeductions,
                p.netSalary,
                p.status,
                p.processedAt,
                p.paidLeaveDays,
                p.unpaidLeaveDays,
                p.leaveDeduction
            from Payroll p
            join p.employee e
            where e.id = ?1
            AND e.hr.id=?2
            AND p.hr.id=?2
            order by p.payPeriodStart desc
            """)
    List<PayrollRespDto> getPayrollByEmployeeIdAndHr(
            @Param("employeeId") Long employeeId,
            Long hrId,
            Pageable pageable
    );


    @Query("""
        SELECT COUNT(p) > 0 FROM Payroll p 
        WHERE p.employee.id = ?1 
        AND p.payPeriodStart = ?2 
        AND p.payPeriodEnd = ?3
        """)
    boolean existsByEmployee_IdAndPayPeriodStartAndPayPeriodEnd(
            Long employeeId,
            LocalDate payPeriodStart,
            LocalDate payPeriodEnd
    );


    Optional<Payroll> findById(Long id);




    // API 2: Filter payrolls by status
    @Query("""
        SELECT p
        FROM Payroll p
        WHERE p.status = ?1
        """)
    List<Payroll> findByStatus(
            PayrollStatus status);

    // API 3: Payroll periods completely inside the requested range
    @Query("""
        SELECT p
        FROM Payroll p
        WHERE p.payPeriodStart >= ?1
          AND p.payPeriodEnd <= ?2
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findByPeriod(
            LocalDate start,
            LocalDate end);

    // API 4: Employee payrolls whose start date is in the range
    @Query("""
        SELECT p
        FROM Payroll p
        WHERE p.employee.id = ?1
          AND p.payPeriodStart BETWEEN ?2 AND ?3
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> searchPayroll(
            Long employeeId,
            LocalDate start,
            LocalDate end);

    // API 7 and 8: Employee payroll history
    @Query("""
        SELECT p
        FROM Payroll p
        WHERE p.employee.id = ?1
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findEmployeePayrollHistory(
            Long employeeId);


    // Monthly report: payroll periods overlapping the requested month
    @Query("""
        SELECT p
        FROM Payroll p
        WHERE p.payPeriodStart <= ?2
          AND p.payPeriodEnd >= ?1
        ORDER BY p.payPeriodStart
        """)
    List<Payroll> findPayrollsOverlappingPeriod(
            LocalDate monthStart,
            LocalDate monthEnd);

    // Find payroll records belonging to a particular HR
    @Query("""
        SELECT p
        FROM Payroll p
        WHERE p.hr.id = ?1
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findPayrollsProcessedByHr(
            Long hrId);


    @Query("""
    SELECT p
    FROM Payroll p
    WHERE p.employee.user.username = ?1
    ORDER BY p.payPeriodStart DESC
    """)
    List<Payroll> findMyPayroll(
            String username);

    // Check whether an employee belongs to the particular HR
    @Query("""
        SELECT COUNT(e) > 0
        FROM Employee e
        WHERE e.id = :employeeId
        AND e.hr.id = :hrId
        """)
    boolean isEmployeeAssignedToHr(
            @Param("employeeId") Long employeeId,
            @Param("hrId") Long hrId
    );


    // Find payroll only when BOTH HR relationships match
    @Query("""
    SELECT p
    FROM Payroll p
    JOIN p.employee e
    WHERE p.id = :payrollId
    AND (p.hr.id = :hrId OR e.hr.id = :hrId)
    """)
    Optional<Payroll> findAuthorizedPayroll(
            @Param("payrollId") Long payrollId,
            @Param("hrId") Long hrId
    );


    // Get payroll history for an employee, restricted to their onboarding HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE e.id = :employeeId
        AND e.hr.id = :hrId
        AND p.hr.id = :hrId
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findEmployeePayrollsForHr(
            @Param("employeeId") Long employeeId,
            @Param("hrId") Long hrId
    );


    // Get all payrolls processed by the logged-in HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE p.hr.id = :hrId
        AND e.hr.id = :hrId
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findAllAuthorizedPayrolls(
            @Param("hrId") Long hrId
    );


    // Filter payrolls by status for a particular HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE p.status = :status
        AND p.hr.id = :hrId
        AND e.hr.id = :hrId
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findByStatusAndHr(
            @Param("status") PayrollStatus status,
            @Param("hrId") Long hrId
    );


    // Filter payrolls by date range for a particular HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE p.payPeriodStart >= :start
        AND p.payPeriodEnd <= :end
        AND p.hr.id = :hrId
        AND e.hr.id = :hrId
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findByPeriodAndHr(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("hrId") Long hrId
    );


    // Search payroll by employee and date range for a particular HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE e.id = :employeeId
        AND p.payPeriodStart BETWEEN :start AND :end
        AND p.hr.id = :hrId
        AND e.hr.id = :hrId
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> searchPayrollForHr(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("hrId") Long hrId
    );


    // Earnings and deductions history restricted to the assigned HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE e.id = :employeeId
        AND e.hr.id = :hrId
        AND p.hr.id = :hrId
        ORDER BY p.payPeriodStart DESC
        """)
    List<Payroll> findEmployeePayrollHistoryForHr(
            @Param("employeeId") Long employeeId,
            @Param("hrId") Long hrId
    );


    // Monthly report restricted to the logged-in HR
    @Query("""
        SELECT p
        FROM Payroll p
        JOIN p.employee e
        WHERE p.payPeriodStart <= :monthEnd
        AND p.payPeriodEnd >= :monthStart
        AND p.hr.id = :hrId
        AND e.hr.id = :hrId
        ORDER BY p.payPeriodStart
        """)
    List<Payroll> findPayrollsOverlappingPeriodForHr(
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("hrId") Long hrId
    );


}


