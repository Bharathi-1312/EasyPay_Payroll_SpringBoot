package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.request.EmployeeDto;
import com.springboot.EasyPay.Dto.request.EmployeeUpdateDto;
import com.springboot.EasyPay.Dto.request.HRUpdateDto;
import com.springboot.EasyPay.Dto.request.ManagerUpdateDto;
import com.springboot.EasyPay.Dto.response.EmployeeInfoByHrDto;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping("api/employee/add")
    public void addEmployeeByHr(Principal principal,
                           @Valid @RequestBody EmployeeDto employeeDto){
        employeeService.addEmployeeByHr(principal.getName(), employeeDto);
    }

    @GetMapping("/api/employee/{id}")
    public Employee getById(@PathVariable long id){ //id=1
        return employeeService.getById(id);
    }

    @PutMapping("/api/employee/{id}")
    public void update(@PathVariable Long id,
                       @RequestBody EmployeeUpdateDto dto){
        employeeService.update(id, dto);
    }


    @GetMapping("/api/employee/get/{hrId}")
    public List<EmployeeInfoByHrDto> getEmployeeInfoByHrId(@PathVariable Long hrId,
                                                           @RequestParam(name = "page" , required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(name = "size", required = false, defaultValue = "20") Integer size){
        return employeeService.getEmployeeInfoByHr(hrId,page,size);
    }

    @PutMapping("api/employee/assign/{managerId}/{employeeId}")
    public void assignManager(@PathVariable Long managerId,
                              @PathVariable Long employeeId,
                              Principal principal)
    {
       employeeService.assignManager(managerId,employeeId,principal.getName());

    }


}