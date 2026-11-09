
package com.springboot.EasyPay.model;

import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
@Getter
@Setter
public class PayrollComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComponentName componentName;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComponentType componentType;

    @Column(nullable = false)
    private BigDecimal amountPercentage;

    @Column(nullable = false)
    private String description;

    @ManyToOne(optional = false)
    private Payroll payroll;

}
