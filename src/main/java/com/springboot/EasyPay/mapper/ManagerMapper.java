package com.springboot.EasyPay.mapper;

import com.springboot.EasyPay.Dto.request.EmployeeDto;
import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.Manager;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class ManagerMapper {
    public static Manager mapDtoToEntity(@Valid ManagerDto managerDto)
    {
        Manager manager=new Manager();
        manager.setName(managerDto.name());
        manager.setEmail(managerDto.email());
        manager.setAddress(managerDto.address());
        manager.setDepartment(managerDto.department());
        manager.setPhone(managerDto.phone());
        return manager;
    }
}
