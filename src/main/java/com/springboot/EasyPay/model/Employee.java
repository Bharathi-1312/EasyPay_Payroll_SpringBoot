package com.springboot.EasyPay.model;

import com.springboot.EasyPay.enums.Department;
import com.springboot.EasyPay.enums.Designation;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
@ToString
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; //[cite: 14]

    private String fullName;
    private String email;
    private String phone;
    private String address;

    @Enumerated(EnumType.STRING)
    private Designation designation;

    @Enumerated(EnumType.STRING)
    private Department department;

    private LocalDate joiningDate;
    private double basicSalary;

    @Column(nullable = false)
    private BigDecimal annualPackage;

    @OneToOne
    private User user;

    @ManyToOne
    private Manager manager;

    @ManyToOne
 private HR hr;

}
