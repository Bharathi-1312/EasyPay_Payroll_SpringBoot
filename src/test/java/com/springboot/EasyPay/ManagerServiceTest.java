package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.ManagerDto;
import com.springboot.EasyPay.Dto.request.ManagerUpdateDto;
import com.springboot.EasyPay.exception.InvalidCallException;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.ManagerMapper;
import com.springboot.EasyPay.model.Manager;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.ManagerRepository;
import com.springboot.EasyPay.repository.UserRepository;
import com.springboot.EasyPay.service.ManagerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    @InjectMocks
    private ManagerService managerService;

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void insertManagerTest() {
        ManagerDto dto = mock(ManagerDto.class);
        User user = new User();
        Manager manager = new Manager();

        when(dto.username()).thenReturn("manager1");
        when(dto.password()).thenReturn("password");
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        try (MockedStatic<ManagerMapper> mapper =
                     mockStatic(ManagerMapper.class)) {

            mapper.when(() -> ManagerMapper.mapDtoToEntity(dto))
                    .thenReturn(manager);

            managerService.insertManager(dto);

            assertSame(user, manager.getUser());
            verify(userRepository).save(any(User.class));
            verify(managerRepository).save(manager);
        }
    }

    @Test
    public void getByIdTest() {
        Manager manager = new Manager();
        when(managerRepository.findById(1L))
                .thenReturn(Optional.of(manager));

        assertSame(manager, managerService.getById(1L));

        verify(managerRepository).findById(1L);
    }

    @Test
    public void getByIdTest_Invalid() {
        when(managerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> managerService.getById(99L));

        verify(managerRepository).findById(99L);
    }

    @Test
    public void updateTest() {
        Manager manager = new Manager();
        ManagerUpdateDto dto = mock(ManagerUpdateDto.class);

        when(managerRepository.getByUsername("manager1"))
                .thenReturn(manager);
        when(dto.name()).thenReturn("Updated Manager");
        when(dto.phone()).thenReturn(null);
        when(dto.address()).thenReturn(null);

        managerService.update("manager1", dto);

        assertEquals("Updated Manager", manager.getName());
        verify(managerRepository).save(manager);
    }

    @Test
    public void updateTest_Invalid() {
        Manager manager = new Manager();
        ManagerUpdateDto dto = mock(ManagerUpdateDto.class);

        when(managerRepository.getByUsername("manager1"))
                .thenReturn(manager);
        when(dto.name()).thenReturn(" ");
        when(dto.phone()).thenReturn(null);
        when(dto.address()).thenReturn(null);

        assertThrows(InvalidCallException.class,
                () -> managerService.update("manager1", dto));

        verify(managerRepository, never()).save(any(Manager.class));
    }
}