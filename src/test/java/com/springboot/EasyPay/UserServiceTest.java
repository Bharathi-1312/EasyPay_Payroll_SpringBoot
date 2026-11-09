package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.AdminDto;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.UserRepository;
import com.springboot.EasyPay.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void getUserObjTest() {
        User savedUser = new User("john", "password", Role.EMPLOYEE);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.getUserObj(
                "john", "password", Role.EMPLOYEE);

        assertSame(savedUser, result);

        verify(userRepository, times(1))
                .save(any(User.class));
    }

    @Test
    public void addAdminTest() {
        AdminDto dto = mock(AdminDto.class);

        when(dto.username()).thenReturn("admin1");
        when(dto.password()).thenReturn("password");
        when(passwordEncoder.encode("password"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.addAdmin(dto);

        verify(passwordEncoder, times(1))
                .encode("password");

        verify(userRepository, times(1)).save(argThat(user ->
                "admin1".equals(user.getUsername())
                        && "encodedPassword".equals(user.getPassword())
                        && user.getRole() == Role.ADMIN));
    }
}