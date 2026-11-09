package com.springboot.EasyPay.controller;

import com.springboot.EasyPay.Dto.response.TokenDto;
import com.springboot.EasyPay.Dto.response.UserDetailsDto;
import com.springboot.EasyPay.Utility.JwtUtility;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.model.Employee;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.EmployeeRepository;
import com.springboot.EasyPay.repository.HRRepository;
import com.springboot.EasyPay.repository.ManagerRepository;
import com.springboot.EasyPay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final JwtUtility jwtUtility;
    private final UserRepository userRepository;
    private final HRRepository hrRepository;
    private final ManagerRepository managerRepository;
    private final EmployeeRepository employeeRepository;

    /* This api is authenticated by Spring. which means if the control reaches this method, Spring
     * already has checked the credentials and it can share the username */
    @GetMapping("/api/auth/login")
    public TokenDto login(Principal principal){
        String loggedInUsername = principal.getName();
        String token = jwtUtility.generateToken(loggedInUsername);
        return new TokenDto(token,
                jwtUtility.extractExpiration(token),
                LocalDateTime.now().toString());
    }

    /* NEW: tells the frontend who the logged-in user is (role + id of the HR/Manager/Employee record).
     * Called with the JWT:  Authorization: Bearer <token> */
    @GetMapping("/api/auth/user-details")
    public UserDetailsDto userDetails(Principal principal){
        User user = userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long profileId = null;
        if (user.getRole() == Role.HR) {
            HR hr = hrRepository.getByUsername(user.getUsername());
            profileId = (hr == null) ? null : hr.getId();
        } else if (user.getRole() == Role.MANAGER) {
            Manager manager = managerRepository.getByUsername(user.getUsername());
            profileId = (manager == null) ? null : manager.getId();
        } else if (user.getRole() == Role.EMPLOYEE) {
            Employee employee = employeeRepository.getByUsername(user.getUsername());
            profileId = (employee == null) ? null : employee.getId();
        }
        return new UserDetailsDto(user.getUsername(), user.getRole().name(), profileId);
    }
}
