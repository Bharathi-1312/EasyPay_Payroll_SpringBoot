package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.LeaveRequestDto;
import com.springboot.EasyPay.Dto.request.LeaveReviewDto;
import com.springboot.EasyPay.Dto.response.LeaveInfoDto;
import com.springboot.EasyPay.enums.Status;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.LeaveRequestMapper;
import com.springboot.EasyPay.mapper.PayrollMapper;
import com.springboot.EasyPay.model.*;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.LeaveRequestRepository;
import com.springboot.EasyPay.repository.ManagerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {
    private final ManagerRepository managerRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;


    public void reqLeaveToManager(String employeeName, @Valid LeaveRequestDto leaveRequestDto) {


        // Get logged-in employee
        Employee employee = employeeRepository
                .findEmployeeByUsername(employeeName)
                .orElseThrow(() ->
                        new RuntimeException("Employee not found"));

        // Validate leave dates
        LocalDate startDate = leaveRequestDto.startDate();
        LocalDate endDate = leaveRequestDto.endDate();

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Leave start date cannot be after end date");
        }

        // Check overlapping leave requests
        long overlappingLeaves =
                leaveRequestRepository.countOverlappingLeaves(
                        employee.getId(),
                        startDate,
                        endDate
                );

        if (overlappingLeaves > 0) {
            throw new IllegalArgumentException(
                    "You already have a leave request for these dates. " +
                            "You cannot apply for overlapping leave dates.");
        }

        LeaveRequest leaveRequest = LeaveRequestMapper.mapDtoToEntity(leaveRequestDto);
        leaveRequest.setEmployee(employee);
        leaveRequest.setManager(employee.getManager());
        leaveRequest.setStatus(Status.PENDING);
        leaveRequestRepository.save(leaveRequest);
    }

    public void reviewLeaveByManager(String managerName,Long leaveId, LeaveReviewDto reviewDto) {

        Manager manager = managerRepository.getByUsername(managerName);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with ID: " + leaveId));

        if (leaveRequest.getStatus() != Status.PENDING) {
            throw new IllegalStateException("This leave request has already been processed.");
        }

        leaveRequest.setStatus(reviewDto.status());
        leaveRequest.setReviewComments(reviewDto.reviewComments());

        leaveRequest.setReviewedAt(LocalDateTime.now());

        leaveRequestRepository.save(leaveRequest);
    }

    // Get all leave requests for the logged-in manager
    public List<LeaveInfoDto> getLeavesForManager(String managerName) {

        Manager manager = managerRepository.getByUsername(managerName);

        return leaveRequestRepository.findByManagerIdOrderByAppliedAtDesc(manager.getId())
                .stream()
                .map(LeaveRequestMapper::mapEntityToDto)
                .toList();
    }


    // Get all leave requests of the logged-in employee
    public List<LeaveInfoDto> getMyLeaves(String username) {

        return leaveRequestRepository.findByEmployeeUsername(username)
                .stream()
                .map(LeaveRequestMapper::mapEntityToDto)
                .toList();
    }



}
