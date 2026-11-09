package com.springboot.EasyPay.Dto.response;

import com.springboot.EasyPay.enums.LeaveType;
import com.springboot.EasyPay.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LeaveInfoDto(Long id, Long employeeId, String employeeName, Long managerId, LeaveType leaveType,
                           LocalDate startDate, LocalDate endDate, String reason, Status status,
                           LocalDateTime appliedAt, LocalDateTime reviewedAt, String reviewComments) {
}
