package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.request.PayrollComponentDto;
import com.springboot.EasyPay.Dto.response.PayrollComponentRespDto;
import com.springboot.EasyPay.enums.ComponentName;
import com.springboot.EasyPay.enums.ComponentType;
import com.springboot.EasyPay.service.PayrollComponentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PayrollComponentController {

    private final PayrollComponentService componentService;

    @PostMapping("/api/payroll-components/{payrollId}")
//    @ResponseStatus(HttpStatus.CREATED)
//    @PreAuthorize("hasRole('HR')")
    public PayrollComponentRespDto addComponent(@PathVariable Long payrollId,
                                                @RequestBody PayrollComponentDto dto) {
        return componentService.addComponent(payrollId,dto);
    }

    @GetMapping("/api/payroll-components/payroll/{payrollId}")
    // @PreAuthorize("hasAnyRole('HR', 'EMPLOYEE')")
    public List<PayrollComponentRespDto> getComponents(
            @PathVariable Long payrollId) {
        return componentService.getComponents(payrollId);
    }

    @DeleteMapping("/api/payroll-components/{componentId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @PreAuthorize("hasRole('HR')")
    public void deleteComponent(
            @PathVariable Long componentId) {
        componentService.deleteComponent(componentId);
    }

    // 16. Components by type
    @GetMapping("/components/type/{type}")
    //@PreAuthorize("hasRole('HR')")
    public List<PayrollComponentRespDto> byType(
            @PathVariable ComponentType type) {
        return componentService.getComponentsByType(type);
    }

    // 17. Components by name
    @GetMapping("/components/name/{name}")
    //@PreAuthorize("hasRole('HR')")
    public List<PayrollComponentRespDto> byName(
            @PathVariable ComponentName name) {
        return componentService.getComponentsByName(name);
    }

    // 15. Update component
    @PatchMapping("/components/{componentId}")
    //@PreAuthorize("hasRole('HR')")
    public PayrollComponentRespDto updateComponent(
            @PathVariable Long componentId,
            @RequestBody PayrollComponentDto dto) {
        return componentService.updateComponent(componentId, dto);
    }
}
