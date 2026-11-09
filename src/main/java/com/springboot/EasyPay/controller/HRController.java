package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.request.HRDto;
import com.springboot.EasyPay.Dto.request.HRUpdateDto;
import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.Dto.request.ManagerUpdateDto;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.service.HRService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class HRController {
    private final HRService hrService;

    @PostMapping("/api/hr/add")
    public void insertHR(@Valid @RequestBody HRDto hrDto){
        hrService.insertHR(hrDto);
    }

    @GetMapping("/api/hr/{id}")
    public HR getById(@PathVariable long id){ //id=1
        return hrService.getById(id);
    }

    @PutMapping("/api/hr")
    public void update(Principal principal,
                       @RequestBody HRUpdateDto dto){
        hrService.update(principal.getName(), dto);
    }
}
