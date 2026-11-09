package com.springboot.EasyPay.mapper;

import com.springboot.EasyPay.Dto.request.EmployeeDto;
import com.springboot.EasyPay.model.Employee;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class EmployeeMapper {
    public static Employee mapDtoToEntity(@Valid EmployeeDto employeeDto) {
        Employee employee = new Employee();
        employee.setFullName(employeeDto.fullName());
        employee.setEmail(employeeDto.email());
        employee.setPhone(employeeDto.phone());
        employee.setAddress(employeeDto.address());
        employee.setDesignation(employeeDto.designation());
        employee.setDepartment(employeeDto.department());
        employee.setJoiningDate(employeeDto.joiningDate());
        employee.setAnnualPackage(employeeDto.annualPackage());
        BigDecimal monthlySalary = employeeDto.annualPackage().divide(BigDecimal.valueOf(12), 2,
                RoundingMode.HALF_UP);

// 2. Convert the result to a double and assign it to the employee
        employee.setBasicSalary(monthlySalary.doubleValue());
        return employee;
    }
}
