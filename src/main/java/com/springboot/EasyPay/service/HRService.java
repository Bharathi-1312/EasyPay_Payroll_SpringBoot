package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.HRDto;
import com.springboot.EasyPay.Dto.request.HRUpdateDto;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.exception.InvalidCallException;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.HRMapper;
import com.springboot.EasyPay.mapper.ManagerMapper;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.HRRepository;
import com.springboot.EasyPay.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j // Lombok annotation to auto-generate the Logger
public class HRService {

    private final UserRepository userRepository;
    private final HRRepository hrRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    public void insertHR(HRDto hrDto) {
        log.info("Initiating HR insertion for username: {}", hrDto.username());

        User user = new User();
        user.setUsername(hrDto.username());
        user.setPassword(passwordEncoder.encode(hrDto.password()));
        user.setRole(Role.HR);
        user = userRepository.save(user); //<-- This user now has an id attached
        log.info("User credentials saved successfully in DB for HR: {}", hrDto.username());

        // Step 2: Map Dto to Entity
        HR hr = HRMapper.mapDtoToEntity(hrDto);
        log.debug("Mapped HRDto to HR entity");

        // Step 3: Attach User to Manager (HR in this context)
        hr.setUser(user);
        log.debug("Attached User to HR entity");

        hrRepository.save(hr);
        log.info("HR profile successfully saved in DB for username: {}", hrDto.username());
    }

    public HR getById(long id) {
        log.info("Fetching HR with ID: {}", id);

        Optional<HR> optional = hrRepository.findById(id);
        if(optional.isEmpty()) {
            log.warn("HR not found in DB with ID: {}", id);
            throw new ResourceNotFoundException("Invalid id");
        }

        log.info("Successfully fetched HR with ID: {}", id);
        return optional.get();
    }

    public void update(String hrName, HRUpdateDto dto) {
        log.info("Attempting to update HR details for username: {}", hrName);

        HR hr = hrRepository.getByUsername(hrName);
        boolean flag = false;

        if(!(dto.name() == null) && !(dto.name().trim().isEmpty())) {    // If dto.name() is NOT(!) empty
            hr.setName(dto.name());
            flag = true;
            log.info("Name updated for HR: {}", hrName);
        }

        if(!(dto.phone() == null) && !(dto.phone().trim().isEmpty())) {    // If dto.phone() is NOT(!) empty
            hr.setPhone(dto.phone());
            flag = true;
            log.info("Phone updated for HR: {}", hrName);
        }

        if(!(dto.address() == null) && !(dto.address().trim().isEmpty())) {
            // Note: I fixed a bug here (was hr.setPhone(dto.phone()) in your original code)
            hr.setAddress(dto.address());
            flag = true;
            log.info("Address updated for HR: {}", hrName);
        }

        if(!flag) {
            log.warn("Update operation terminated for HR: {}. No valid data provided in DTO.", hrName);
            throw new InvalidCallException("Update op terminated");
        }

        hrRepository.save(hr);
        log.info("HR details successfully updated in DB for username: {}", hrName);
    }
}