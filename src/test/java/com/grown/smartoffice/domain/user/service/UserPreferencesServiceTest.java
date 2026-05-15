package com.grown.smartoffice.domain.user.service;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.dto.UserPreferencesResponse;
import com.grown.smartoffice.domain.user.dto.UserPreferencesUpdateRequest;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserPreferences;
import com.grown.smartoffice.domain.user.repository.UserPreferencesRepository;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import com.grown.smartoffice.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock UserPreferencesRepository userPreferencesRepository;
    @Mock UserRepository userRepository;
    @InjectMocks UserPreferencesService userPreferencesService;

    private User user;

    @BeforeEach
    void setUp() {
        Department dept = TestFixtures.department(1L, "개발팀");
        user = TestFixtures.adminUser(1L, "admin@grown.com", dept);
    }

    private UserPreferencesUpdateRequest updateRequest(Boolean noti, String lang,
                                                       String theme, String pushToken) {
        UserPreferencesUpdateRequest req = new UserPreferencesUpdateRequest();
        ReflectionTestUtils.setField(req, "notificationsEnabled", noti);
        ReflectionTestUtils.setField(req, "language", lang);
        ReflectionTestUtils.setField(req, "theme", theme);
        ReflectionTestUtils.setField(req, "pushToken", pushToken);
        return req;
    }

    @Test
    @DisplayName("환경설정 조회 — 행이 없으면 기본값으로 생성 후 반환")
    void getMyPreferences_lazyCreatesDefaults() {
        given(userRepository.findByEmployeeEmail("admin@grown.com")).willReturn(Optional.of(user));
        given(userPreferencesRepository.findById(1L)).willReturn(Optional.empty());
        given(userPreferencesRepository.save(any(UserPreferences.class)))
                .willAnswer(inv -> inv.getArgument(0));

        UserPreferencesResponse res = userPreferencesService.getMyPreferences("admin@grown.com");

        assertThat(res.isNotificationsEnabled()).isTrue();
        assertThat(res.getLanguage()).isEqualTo("ko");
        assertThat(res.getTheme()).isEqualTo("light");
        verify(userPreferencesRepository).save(any(UserPreferences.class));
    }

    @Test
    @DisplayName("환경설정 조회 — 행이 있으면 저장 없이 반환")
    void getMyPreferences_returnsExisting() {
        UserPreferences existing = UserPreferences.builder()
                .userId(1L).notificationsEnabled(false).language("en").theme("dark").build();
        given(userRepository.findByEmployeeEmail("admin@grown.com")).willReturn(Optional.of(user));
        given(userPreferencesRepository.findById(1L)).willReturn(Optional.of(existing));

        UserPreferencesResponse res = userPreferencesService.getMyPreferences("admin@grown.com");

        assertThat(res.getLanguage()).isEqualTo("en");
        assertThat(res.getTheme()).isEqualTo("dark");
        verify(userPreferencesRepository, never()).save(any());
    }

    @Test
    @DisplayName("환경설정 수정 — null 필드는 기존 값 유지 (부분 수정)")
    void updateMyPreferences_partialUpdate() {
        UserPreferences existing = UserPreferences.builder()
                .userId(1L).notificationsEnabled(true).language("ko").theme("light").build();
        given(userRepository.findByEmployeeEmail("admin@grown.com")).willReturn(Optional.of(user));
        given(userPreferencesRepository.findById(1L)).willReturn(Optional.of(existing));

        UserPreferencesResponse res = userPreferencesService.updateMyPreferences(
                "admin@grown.com", updateRequest(false, null, "dark", null));

        assertThat(res.isNotificationsEnabled()).isFalse();
        assertThat(res.getLanguage()).isEqualTo("ko");   // null → 유지
        assertThat(res.getTheme()).isEqualTo("dark");     // 변경
    }

    @Test
    @DisplayName("환경설정 조회 — 사용자 부재 시 USER_NOT_FOUND")
    void getMyPreferences_userNotFound() {
        given(userRepository.findByEmployeeEmail("ghost@grown.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userPreferencesService.getMyPreferences("ghost@grown.com"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
