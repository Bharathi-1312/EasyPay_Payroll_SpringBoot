package com.springboot.EasyPay.service;

import com.springboot.EasyPay.Dto.request.AdminDto;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserObj(String username, String password, Role role){
        User user =  new User(username,password,role);
        return userRepository.save(user);
    }
    public void addAdmin(AdminDto adminDto) {
        // Prepare User using AdminDto
        User user = new User(
                adminDto.username(),
                passwordEncoder.encode(adminDto.password()),
                Role.ADMIN
        );

        // Save user in DB
        userRepository.save(user);
    }
}
