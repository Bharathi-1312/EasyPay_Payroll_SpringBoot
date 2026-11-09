package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.request.LeaveRequestDto;
import com.springboot.EasyPay.Dto.request.LeaveReviewDto;
import com.springboot.EasyPay.Dto.request.PayrollDto;
import com.springboot.EasyPay.Dto.response.LeaveInfoDto;
import com.springboot.EasyPay.model.LeaveRequest;
import com.springboot.EasyPay.service.LeaveRequestService;
import com.springboot.EasyPay.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor

@CrossOrigin(origins = "http://localhost:5173")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @PostMapping("api/leavereq/apply")
    public void reqLeaveToManager(Principal principal,
                                  @Valid @RequestBody LeaveRequestDto leaveRequestDto) {
        leaveRequestService.reqLeaveToManager( principal.getName(),leaveRequestDto);
    }

    @PutMapping("api/reviewleave/{leaveId}")
    public void reviewLeaveByManager(
         Principal principal,
            @PathVariable Long leaveId,
            @Valid @RequestBody LeaveReviewDto leaveReviewDto
    ){
        leaveRequestService.reviewLeaveByManager(principal.getName(),leaveId, leaveReviewDto);
    }

    // Get all leave requests assigned to logged-in manager
    @GetMapping("/api/leavereq/manager")
    public ResponseEntity<List<LeaveInfoDto>> getLeavesForManager(
            Principal principal) {

        return ResponseEntity.ok(
                leaveRequestService.getLeavesForManager(
                        principal.getName()
                )
        );
    }


    // Get all leave requests of logged-in employee
    @GetMapping("/api/leavereq/employee")
    public ResponseEntity<List<LeaveInfoDto>> getMyLeaves(
            Principal principal) {

        return ResponseEntity.ok(
                leaveRequestService.getMyLeaves(
                        principal.getName()
                )
        );
    }


}
