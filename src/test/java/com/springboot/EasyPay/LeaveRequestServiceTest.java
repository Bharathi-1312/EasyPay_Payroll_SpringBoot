package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.LeaveRequestDto;
import com.springboot.EasyPay.Dto.request.LeaveReviewDto;
import com.springboot.EasyPay.Dto.response.LeaveInfoDto;
import com.springboot.EasyPay.enums.Status;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.LeaveRequestMapper;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.LeaveRequest;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.LeaveRequestRepository;
import com.springboot.EasyPay.repository.ManagerRepository;
import com.springboot.EasyPay.service.LeaveRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    // ==================================================
    // 1. REQUEST LEAVE TO MANAGER
    // ==================================================

    @Test
    void reqLeaveToManagerTest_Success() {
        Employee employee = new Employee();
        employee.setId(1L);
        Manager manager = new Manager();
        employee.setManager(manager);

        LeaveRequestDto dto = mock(LeaveRequestDto.class);
        LeaveRequest leaveRequest = new LeaveRequest();

        LocalDate startDate = LocalDate.of(2026, 11, 10);
        LocalDate endDate = LocalDate.of(2026, 11, 15);

        when(dto.startDate()).thenReturn(startDate);
        when(dto.endDate()).thenReturn(endDate);

        when(employeeRepository.findEmployeeByUsername("employee1"))
                .thenReturn(Optional.of(employee));

        when(leaveRequestRepository.countOverlappingLeaves(1L, startDate, endDate))
                .thenReturn(0L);

        try (MockedStatic<LeaveRequestMapper> mapper = mockStatic(LeaveRequestMapper.class)) {
            mapper.when(() -> LeaveRequestMapper.mapDtoToEntity(dto))
                    .thenReturn(leaveRequest);

            leaveRequestService.reqLeaveToManager("employee1", dto);

            assertSame(employee, leaveRequest.getEmployee());
            assertSame(manager, leaveRequest.getManager());
            assertEquals(Status.PENDING, leaveRequest.getStatus());

            verify(leaveRequestRepository).save(leaveRequest);
        }
    }

    @Test
    void reqLeaveToManagerTest_EmployeeNotFound() {
        when(employeeRepository.findEmployeeByUsername("employee1"))
                .thenReturn(Optional.empty());

        LeaveRequestDto dto = mock(LeaveRequestDto.class);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leaveRequestService.reqLeaveToManager("employee1", dto));

        assertEquals("Employee not found", exception.getMessage());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void reqLeaveToManagerTest_InvalidDates() {
        Employee employee = new Employee();
        when(employeeRepository.findEmployeeByUsername("employee1"))
                .thenReturn(Optional.of(employee));

        LeaveRequestDto dto = mock(LeaveRequestDto.class);
        when(dto.startDate()).thenReturn(LocalDate.of(2026, 11, 20));
        when(dto.endDate()).thenReturn(LocalDate.of(2026, 11, 10)); // End date before start date

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.reqLeaveToManager("employee1", dto));

        assertEquals("Leave start date cannot be after end date", exception.getMessage());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void reqLeaveToManagerTest_OverlappingLeaves() {
        Employee employee = new Employee();
        employee.setId(1L);

        when(employeeRepository.findEmployeeByUsername("employee1"))
                .thenReturn(Optional.of(employee));

        LeaveRequestDto dto = mock(LeaveRequestDto.class);
        LocalDate startDate = LocalDate.of(2026, 11, 10);
        LocalDate endDate = LocalDate.of(2026, 11, 15);
        when(dto.startDate()).thenReturn(startDate);
        when(dto.endDate()).thenReturn(endDate);

        when(leaveRequestRepository.countOverlappingLeaves(1L, startDate, endDate))
                .thenReturn(1L); // Simulating an overlap exists

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.reqLeaveToManager("employee1", dto));

        assertTrue(exception.getMessage().contains("You already have a leave request for these dates"));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    // ==================================================
    // 2. REVIEW LEAVE BY MANAGER
    // ==================================================

    @Test
    void reviewLeaveByManagerTest_Success() {
        Manager manager = new Manager();
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStatus(Status.PENDING);

        LeaveReviewDto reviewDto = mock(LeaveReviewDto.class);
        when(reviewDto.status()).thenReturn(Status.APPROVED);
        when(reviewDto.reviewComments()).thenReturn("Approved, enjoy your leave.");

        when(managerRepository.getByUsername("manager1")).thenReturn(manager);
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

        leaveRequestService.reviewLeaveByManager("manager1", 100L, reviewDto);

        assertEquals(Status.APPROVED, leaveRequest.getStatus());
        assertEquals("Approved, enjoy your leave.", leaveRequest.getReviewComments());
        assertNotNull(leaveRequest.getReviewedAt());
        verify(leaveRequestRepository).save(leaveRequest);
    }

    @Test
    void reviewLeaveByManagerTest_LeaveNotFound() {
        LeaveReviewDto reviewDto = mock(LeaveReviewDto.class);
        when(managerRepository.getByUsername("manager1")).thenReturn(new Manager());
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> leaveRequestService.reviewLeaveByManager("manager1", 100L, reviewDto));

        assertEquals("Leave request not found with ID: 100", exception.getMessage());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void reviewLeaveByManagerTest_AlreadyProcessed() {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStatus(Status.APPROVED); // Already processed

        LeaveReviewDto reviewDto = mock(LeaveReviewDto.class);

        when(managerRepository.getByUsername("manager1")).thenReturn(new Manager());
        when(leaveRequestRepository.findById(100L)).thenReturn(Optional.of(leaveRequest));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> leaveRequestService.reviewLeaveByManager("manager1", 100L, reviewDto));

        assertEquals("This leave request has already been processed.", exception.getMessage());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    // ==================================================
    // 3. GET LEAVES FOR MANAGER
    // ==================================================

    @Test
    void getLeavesForManagerTest() {
        Manager manager = new Manager();
        manager.setId(10L);

        LeaveRequest leaveRequest = new LeaveRequest();
        LeaveInfoDto infoDto = mock(LeaveInfoDto.class);

        when(managerRepository.getByUsername("manager1")).thenReturn(manager);
        when(leaveRequestRepository.findByManagerIdOrderByAppliedAtDesc(10L))
                .thenReturn(List.of(leaveRequest));

        try (MockedStatic<LeaveRequestMapper> mapper = mockStatic(LeaveRequestMapper.class)) {
            mapper.when(() -> LeaveRequestMapper.mapEntityToDto(leaveRequest))
                    .thenReturn(infoDto);

            List<LeaveInfoDto> result = leaveRequestService.getLeavesForManager("manager1");

            assertEquals(1, result.size());
            assertSame(infoDto, result.get(0));
            verify(leaveRequestRepository).findByManagerIdOrderByAppliedAtDesc(10L);
        }
    }

    // ==================================================
    // 4. GET MY LEAVES
    // ==================================================

    @Test
    void getMyLeavesTest() {
        LeaveRequest leaveRequest = new LeaveRequest();
        LeaveInfoDto infoDto = mock(LeaveInfoDto.class);

        when(leaveRequestRepository.findByEmployeeUsername("employee1"))
                .thenReturn(List.of(leaveRequest));

        try (MockedStatic<LeaveRequestMapper> mapper = mockStatic(LeaveRequestMapper.class)) {
            mapper.when(() -> LeaveRequestMapper.mapEntityToDto(leaveRequest))
                    .thenReturn(infoDto);

            List<LeaveInfoDto> result = leaveRequestService.getMyLeaves("employee1");

            assertEquals(1, result.size());
            assertSame(infoDto, result.get(0));
            verify(leaveRequestRepository).findByEmployeeUsername("employee1");
        }
    }
}