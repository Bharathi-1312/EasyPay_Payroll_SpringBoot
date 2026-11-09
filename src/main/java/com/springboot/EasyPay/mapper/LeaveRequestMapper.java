package com.springboot.EasyPay.mapper;

import com.springboot.EasyPay.Dto.request.LeaveRequestDto;
import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.Dto.response.LeaveInfoDto;
import com.springboot.EasyPay.enums.Status;
import com.springboot.EasyPay.model.LeaveRequest;
import com.springboot.EasyPay.model.Manager;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class LeaveRequestMapper {
    public static LeaveRequest mapDtoToEntity(@Valid LeaveRequestDto dto) {
        LeaveRequest entity = new LeaveRequest();
        entity.setLeaveType(dto.leaveType());
        entity.setStartDate(dto.startDate());
        entity.setEndDate(dto.endDate());
        entity.setReason(dto.reason());
        entity.setMedicalCertificateSubmitted(dto.medicalCertificateSubmitted());
        return entity;
    }

    public static LeaveInfoDto mapEntityToDto(LeaveRequest leave) {

        return new LeaveInfoDto(
                leave.getId(),
                leave.getEmployee().getId(),
                leave.getEmployee().getFullName(),
                leave.getManager() != null ? leave.getManager().getId() : null,
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                leave.getAppliedAt(),
                leave.getReviewedAt(),
                leave.getReviewComments()
        );
    }
}
