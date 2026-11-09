package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.Department;
import com.springboot.EasyPay.enums.Designation;

import java.time.LocalDate;

public record EmployeeInfoByHrDto(
        Long id,String fullName,
                                  String email,
                                  String phone,
                                  String address,
                                  Designation designation,
                                  Department department,
                                  LocalDate joiningDate,
                                  double basicSalary,
                                  String username,
                                  String name) {
}
