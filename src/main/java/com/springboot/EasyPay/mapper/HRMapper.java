package com.springboot.EasyPay.mapper;

import com.springboot.EasyPay.Dto.request.HRDto;
import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class HRMapper {
    public static HR mapDtoToEntity(@Valid HRDto hrDto)
    {
        HR hr=new HR();
        hr.setName(hrDto.name());
        hr.setEmail(hrDto.email());
        hr.setDepartment(hrDto.department());
        hr.setPhone(hrDto.phone());
        hr.setAddress(hrDto.address());
        return hr;
    }
}
