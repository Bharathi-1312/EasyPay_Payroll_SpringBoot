package com.springboot.EasyPay;

import com.springboot.EasyPay.Dto.request.HRDto;
import com.springboot.EasyPay.Dto.request.HRUpdateDto;
import com.springboot.EasyPay.enums.Role;
import com.springboot.EasyPay.exception.InvalidCallException;
import com.springboot.EasyPay.exception.ResourceNotFoundException;
import com.springboot.EasyPay.mapper.HRMapper;
import com.springboot.EasyPay.model.HR;
import com.springboot.EasyPay.model.User;
import com.springboot.EasyPay.repository.HRRepository;
import com.springboot.EasyPay.repository.UserRepository;
import com.springboot.EasyPay.service.HRService;
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
public class HRServiceTest {

    @InjectMocks
    private HRService hrService;

    @Mock
    private HRRepository hrRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void insertHRTest() {
        HRDto dto = mock(HRDto.class);
        User user = new User();
        HR hr = new HR();

        when(dto.username()).thenReturn("hr1");
        when(dto.password()).thenReturn("password");
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        try (MockedStatic<HRMapper> mapper = mockStatic(HRMapper.class)) {
            mapper.when(() -> HRMapper.mapDtoToEntity(dto)).thenReturn(hr);

            hrService.insertHR(dto);

            assertSame(user, hr.getUser());
            verify(userRepository).save(any(User.class));
            verify(hrRepository).save(hr);
        }
    }

    @Test
    public void getByIdTest() {
        HR hr = new HR();
        when(hrRepository.findById(1L)).thenReturn(Optional.of(hr));

        assertSame(hr, hrService.getById(1L));

        verify(hrRepository).findById(1L);
    }

    @Test
    public void getByIdTest_Invalid() {
        when(hrRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> hrService.getById(99L));

        verify(hrRepository).findById(99L);
    }

    @Test
    public void updateTest() {
        HR hr = new HR();
        HRUpdateDto dto = mock(HRUpdateDto.class);

        when(hrRepository.getByUsername("hr1")).thenReturn(hr);
        when(dto.name()).thenReturn("Updated HR");
        when(dto.phone()).thenReturn(null);
        when(dto.address()).thenReturn(null);

        hrService.update("hr1", dto);

        assertEquals("Updated HR", hr.getName());
        verify(hrRepository).save(hr);
    }

    @Test
    public void updateTest_Invalid() {
        HR hr = new HR();
        HRUpdateDto dto = mock(HRUpdateDto.class);

        when(hrRepository.getByUsername("hr1")).thenReturn(hr);
        when(dto.name()).thenReturn(" ");
        when(dto.phone()).thenReturn(null);
        when(dto.address()).thenReturn(null);

        assertThrows(InvalidCallException.class,
                () -> hrService.update("hr1", dto));

        verify(hrRepository, never()).save(any(HR.class));
    }
}