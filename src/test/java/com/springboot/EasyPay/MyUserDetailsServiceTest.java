package com.springboot.EasyPay;

import com.springboot.EasyPay.repository.UserRepository;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.service.MyUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MyUserDetailsServiceTest {

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void loadUserByUsernameTest() {
        User user = mock(User.class);

        when(userRepository.loadUserByUsername("john", true))
                .thenReturn(user);

        UserDetails result =
                myUserDetailsService.loadUserByUsername("john");

        assertSame(user, result);
        verify(userRepository, times(1))
                .loadUserByUsername("john", true);
        verifyNoMoreInteractions(userRepository);
    }
}