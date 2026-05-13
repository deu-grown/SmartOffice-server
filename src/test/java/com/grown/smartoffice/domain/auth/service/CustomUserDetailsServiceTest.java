package com.grown.smartoffice.domain.auth.service;

import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername — 이메일로 User 반환 (UserDetails 구현체)")
    void loadByEmail_success() {
        User user = User.builder()
                .employeeNumber("EMP-CU").employeeName("커스텀")
                .employeeEmail("cu@grown.com").password("pw")
                .role(UserRole.USER).position("사원")
                .status(UserStatus.ACTIVE).hiredAt(LocalDate.now()).build();
        given(userRepository.findByEmployeeEmail("cu@grown.com")).willReturn(Optional.of(user));

        UserDetails ud = customUserDetailsService.loadUserByUsername("cu@grown.com");
        assertThat(ud).isNotNull();
        assertThat(ud.getUsername()).isEqualTo("cu@grown.com");
    }

    @Test
    @DisplayName("loadUserByUsername — 미존재 → UsernameNotFoundException")
    void loadByEmail_notFound() {
        given(userRepository.findByEmployeeEmail("none@grown.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("none@grown.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}
