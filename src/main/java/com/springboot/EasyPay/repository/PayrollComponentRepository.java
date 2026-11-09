package com.springboot.EasyPay.repository;

import com.springboot.EasyPay.Dto.response.PayrollComponentRespDto;
import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import com.springboot.EasyPay.model.PayrollComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollComponentRepository
        extends JpaRepository<PayrollComponent, Long> {

        @Query("""
            select new com.springboot.EasyPay.Dto.response.PayrollComponentRespDto(
                c.id,
                p.id,
                c.componentName ,
                c.componentType,
                c.amountPercentage,
                c.description
            )
            from PayrollComponent c
            join c.payroll p
            where p.id = ?1
            order by c.id
            """)
        List<PayrollComponentRespDto> getComponentsByPayrollId(
                @Param("payrollId") Long payrollId
        );

        @Query("""
        SELECT pc
        FROM PayrollComponent pc
        WHERE pc.componentType = ?1
        ORDER BY pc.id DESC
        """)
        List<PayrollComponent> findByComponentType(
                ComponentType type);

        @Query("""
        SELECT pc
        FROM PayrollComponent pc
        WHERE pc.componentName = ?1
        ORDER BY pc.id DESC
        """)
        List<PayrollComponent> findByComponentName(
                ComponentName name);

        @Query("""
        SELECT pc
        FROM PayrollComponent pc
        WHERE pc.payroll.id = ?1
        ORDER BY pc.id
        """)
        List<PayrollComponent> findByPayrollId(
                Long payrollId);
}
