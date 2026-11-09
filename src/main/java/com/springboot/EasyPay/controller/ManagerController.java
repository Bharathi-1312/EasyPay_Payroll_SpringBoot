package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.Dto.request.ManagerUpdateDto;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ManagerController {
    private  final ManagerService managerService;

    @PostMapping("/api/manager/add")
    public void insertManager(@Valid @RequestBody ManagerDto managerDto){
        managerService.insertManager(managerDto);
    }

    @GetMapping("/api/manager/{id}") ///api/customer/1
    public Manager getById(@PathVariable long id){ //id=1
        return managerService.getById(id);
    }

    @PutMapping("/api/manager")
    public void update(Principal principal,
                       @RequestBody ManagerUpdateDto dto){
        managerService.update(principal.getName(), dto);
    }

}
