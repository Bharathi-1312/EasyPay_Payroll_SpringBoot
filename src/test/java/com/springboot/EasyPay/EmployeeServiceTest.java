package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.EmployeeDto;
import com.springboot.EasyPay.Dto.request.EmployeeUpdateDto;
import com.springboot.EasyPay.Dto.response.EmployeeInfoByHrDto;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.exception.InvalidCallException;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.EmployeeMapper;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.HRRepository;
import com.springboot.EasyPay.repository.ManagerRepository;
import com.springboot.EasyPay.repository.UserRepository;
import com.springboot.EasyPay.service.EmployeeService;
import com.springboot.EasyPay.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @InjectMocks
    private EmployeeService employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private HRRepository hrRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Test
    public void getByIdTest() {
        Employee employee = new Employee();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        assertSame(employee, employeeService.getById(1L));

        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    public void getByIdTest_Invalid() {
        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getById(99L));

        verify(employeeRepository, times(1)).findById(99L);
        verifyNoMoreInteractions(employeeRepository);
    }

    @Test
    public void addEmployeeByHrTest_NewUser() {
        EmployeeDto dto = mock(EmployeeDto.class);
        HR hr = new HR();
        User user = mock(User.class);
        Employee employee = new Employee();

        when(hrRepository.getByUsername("hr1")).thenReturn(hr);
        when(dto.username()).thenReturn("employee1");
        when(dto.password()).thenReturn("password");

        // MOCK THE REPOSITORY TO RETURN EMPTY (Simulating a new user)
        when(userRepository.findUserByUsername("employee1")).thenReturn(Optional.empty());

        when(userService.getUserObj("employee1", "password", Role.EMPLOYEE))
                .thenReturn(user);
        when(user.getPassword()).thenReturn("password");
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        try (MockedStatic<EmployeeMapper> mapper =
                     mockStatic(EmployeeMapper.class)) {

            mapper.when(() -> EmployeeMapper.mapDtoToEntity(dto))
                    .thenReturn(employee);

            employeeService.addEmployeeByHr("hr1", dto);

            assertSame(hr, employee.getHr());
            assertSame(user, employee.getUser());

            // Verify new user steps occurred
            verify(passwordEncoder).encode("password");
            verify(user).setPassword("encoded");
            verify(employeeRepository).save(employee);
        }
    }

    @Test
    public void addEmployeeByHrTest_ExistingUser() {
        EmployeeDto dto = mock(EmployeeDto.class);
        HR hr = new HR();
        User user = mock(User.class);
        Employee employee = new Employee();

        when(hrRepository.getByUsername("hr1")).thenReturn(hr);
        when(dto.username()).thenReturn("employee1");

        // MOCK THE REPOSITORY TO RETURN A USER (Simulating an existing user)
        when(userRepository.findUserByUsername("employee1")).thenReturn(Optional.of(user));

        try (MockedStatic<EmployeeMapper> mapper =
                     mockStatic(EmployeeMapper.class)) {

            mapper.when(() -> EmployeeMapper.mapDtoToEntity(dto))
                    .thenReturn(employee);

            employeeService.addEmployeeByHr("hr1", dto);

            assertSame(hr, employee.getHr());
            assertSame(user, employee.getUser());

            // Verify new user creation & encoding was SKIPPED
            verify(userService, never()).getUserObj(anyString(), anyString(), any());
            verify(passwordEncoder, never()).encode(anyString());

            verify(employeeRepository).save(employee);
        }
    }

    @Test
    public void updateEmployeeTest() {
        Employee employee = new Employee();
        EmployeeUpdateDto dto = mock(EmployeeUpdateDto.class);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(dto.fullname()).thenReturn("Updated Name");
        when(dto.phone()).thenReturn(null);
        when(dto.address()).thenReturn(null);

        employeeService.update(1L, dto);

        assertEquals("Updated Name", employee.getFullName());
        verify(employeeRepository).save(employee);
    }

    @Test
    public void updateEmployeeTest_Invalid() {
        Employee employee = new Employee();
        EmployeeUpdateDto dto = mock(EmployeeUpdateDto.class);

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));
        when(dto.fullname()).thenReturn(" ");
        when(dto.phone()).thenReturn(null);
        when(dto.address()).thenReturn(null);

        assertThrows(InvalidCallException.class,
                () -> employeeService.update(1L, dto));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void getEmployeeInfoByHrTest() {
        HR hr = new HR();
        List<EmployeeInfoByHrDto> employees = List.of(
                mock(EmployeeInfoByHrDto.class),
                mock(EmployeeInfoByHrDto.class));

        when(hrRepository.findById(1L)).thenReturn(Optional.of(hr));
        when(employeeRepository.getEmployeeByHrId(
                eq(1L), eq(PageRequest.of(0, 5))))
                .thenReturn(employees);

        assertEquals(2,
                employeeService.getEmployeeInfoByHr(1L, 0, 5).size());

        verify(hrRepository).findById(1L);
        verify(employeeRepository).getEmployeeByHrId(
                1L, PageRequest.of(0, 5));
    }

    @Test
    public void getEmployeeInfoByHrTest_Invalid() {
        when(hrRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.getEmployeeInfoByHr(99L, 0, 5));

        verify(employeeRepository, never())
                .getEmployeeByHrId(eq(99L), any());
    }

    @Test
    public void assignManagerTest() {
        Manager manager = new Manager();
        Employee employee = new Employee();

        when(managerRepository.findById(2L))
                .thenReturn(Optional.of(manager));
        when(hrRepository.getByUsername("hr1"))
                .thenReturn(new HR());
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        employeeService.assignManager(2L, 1L, "hr1");

        assertSame(manager, employee.getManager());
        verify(employeeRepository).save(employee);
    }

    @Test
    public void assignManagerTest_ManagerNotFound() {
        when(managerRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.assignManager(2L, 1L, "hr1"));

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    public void assignManagerTest_EmployeeNotFound() {
        when(managerRepository.findById(2L))
                .thenReturn(Optional.of(new Manager()));
        when(hrRepository.getByUsername("hr1"))
                .thenReturn(new HR());
        when(employeeRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeService.assignManager(2L, 1L, "hr1"));

        verify(employeeRepository, never()).save(any(Employee.class));
    }
}
