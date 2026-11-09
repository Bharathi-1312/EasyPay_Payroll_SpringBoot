package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.EmployeeDto;
import com.springboot.EasyPay.Dto.request.EmployeeUpdateDto;
import com.springboot.EasyPay.Dto.request.HRUpdateDto;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private  final EmployeeRepository employeeRepository;
    private final HRRepository hrRepository;
    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

//    public void addEmployeeByHr(String hrName,EmployeeDto employeeDto) {
//
//        HR hr =  hrRepository.getByUsername(hrName);
//
//        User user = userService.getUserObj(
//                employeeDto.username(),
//                employeeDto.password(),
//                Role.EMPLOYEE
//        );
//
//        // Step 1.5 : Encode the password
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//        Employee employee = EmployeeMapper.mapDtoToEntity(employeeDto);
//
//        employee.setUser(user);
//        employee.setHr(hr);
//
//        employeeRepository.save(employee);
//    }


    public void addEmployeeByHr(String hrName, EmployeeDto employeeDto) {

        HR hr = hrRepository.getByUsername(hrName);

        // Step 1: Check if the user already exists in the database
        // (Replace userService.findByUsername with your actual method name)
        Optional<User> existingUser = userRepository.findUserByUsername(employeeDto.username());

        User user;

        if (existingUser.isPresent()) {
            // Step 2: If the user exists, use that existing record
            user = existingUser.get();

            // Optional: Update the password if provided/changed, or verify it matches
            // user.setPassword(passwordEncoder.encode(employeeDto.password()));
        } else {
            // Step 3: If the user does not exist, create a new one with EMPLOYEE role
            user = userService.getUserObj(
                    employeeDto.username(),
                    employeeDto.password(),
                    Role.EMPLOYEE
            );
            // Encode the password only for the new user
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Step 4: Map and link entities
        Employee employee = EmployeeMapper.mapDtoToEntity(employeeDto);
        employee.setUser(user);
        employee.setHr(hr);

        // Step 5: Save the employee profile
        employeeRepository.save(employee);
    }


    public Employee getById(long id) {
        Optional<Employee> optional= employeeRepository.findById(id);
        if(optional.isEmpty())
        {
            throw new ResourceNotFoundException("Invalid id");
        }
        return optional.get();
    }
    public void update(Long id, EmployeeUpdateDto dto) {

            Employee employee = getById(id);
            boolean flag = false;

            if (!(dto.fullname() == null) && !(dto.fullname().trim().isEmpty())) {    // If dto.name() is NOT(!) empty
                employee.setFullName(dto.fullname());
                flag = true;
            }

            if (!(dto.phone() == null) && !(dto.phone().trim().isEmpty())) {    // If dto.name() is NOT(!) empty
                employee.setPhone(dto.phone());
                flag = true;
            }

            if (!(dto.address() == null) && !(dto.address().trim().isEmpty())) {
                employee.setPhone(dto.phone());
                flag = true;
            }

            if (!flag)
                throw new InvalidCallException("Update op terminated");

            employeeRepository.save(employee);

    }

    public List<EmployeeInfoByHrDto> getEmployeeInfoByHr(Long hrId, Integer page, Integer size) {
// Validate customerId
        HR hr = hrRepository.findById(hrId)
                .orElseThrow(() -> new ResourceNotFoundException("HR Id Invalid"));


        // Create an Instance of Pageable interface using page and size values thru PageRequest
        Pageable pageable =  PageRequest.of(page,size);
        return employeeRepository.getEmployeeByHrId(hrId,pageable);
    }

    public void assignManager(Long managerId,Long employeeId,String hrName) {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(()-> new ResourceNotFoundException("Manager not found"));


        HR hr =  hrRepository.getByUsername(hrName);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(()-> new ResourceNotFoundException("Employee not found"));

        employee.setManager(manager);
        employeeRepository.save(employee);
    }




}
