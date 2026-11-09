package com.springboot.EasyPay.model;

import com.springboot.EasyPay.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
@Getter
@Setter
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate payPeriodStart;

    private LocalDate payPeriodEnd;


    private BigDecimal basicSalary;


    private BigDecimal totalEarnings;


    private BigDecimal totalDeductions;


    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status; // DRAFT, PROCESSED, PAID

    private LocalDateTime processedAt;

    private BigDecimal leaveDeduction = BigDecimal.ZERO;

    private int paidLeaveDays;

    private int unpaidLeaveDays;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "hr_id")
    private HR hr;

    @OneToMany(mappedBy = "payroll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PayrollComponent> components = new ArrayList<>();

    public void addComponent(PayrollComponent component) {
        component.setPayroll(this);
        components.add(component);
    }

}
