package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.Dto.request.ManagerUpdateDto;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.exception.InvalidCallException;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.ManagerMapper;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.ManagerRepository;
import com.springboot.EasyPay.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private  final ManagerRepository managerRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeRepository employeeRepository;

    public void insertManager(ManagerDto managerDto) {
        User user = new User();
        user.setUsername(managerDto.username());
        user.setPassword(passwordEncoder.encode(managerDto.password()));
        user.setRole(Role.MANAGER);
        user = userRepository.save(user); //<-- This user now has an id attached

        // Step 2: Map Dto to Entity
        Manager manager = ManagerMapper.mapDtoToEntity(managerDto);

        // Step 3: Attach User to Manager
        manager.setUser(user);

        // Step 4: save manager
        managerRepository.save(manager);
    }

    public Manager getById(long id) {
        Optional<Manager> optional= managerRepository.findById(id);
        if(optional.isEmpty())
        {
            throw new ResourceNotFoundException("Invalid id");
        }
        return optional.get();
    }

    public void update(String managerName, ManagerUpdateDto dto) {
        Manager manager =  managerRepository.getByUsername(managerName);
        boolean flag = false;

        if(!(dto.name() == null) && !(dto.name().trim().isEmpty())) {
            manager.setName(dto.name());
            flag = true;
        }

        if(!(dto.phone() == null) && !(dto.phone().trim().isEmpty())) {
            manager.setPhone(dto.phone());
            flag = true;
        }

        if(!(dto.address() == null) && !(dto.address().trim().isEmpty())) {
            manager.setPhone(dto.phone());
            flag = true;
        }

        if(!flag)
            throw new InvalidCallException("Update op terminated");

        managerRepository.save(manager);
    }
}
