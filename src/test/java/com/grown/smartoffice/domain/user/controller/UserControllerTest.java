package com.grown.smartoffice.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.accesslog.dto.UserAccessLogListResponse;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import com.grown.smartoffice.domain.user.dto.*;
import com.grown.smartoffice.domain.user.service.UserPreferencesService;
import com.grown.smartoffice.domain.user.service.UserService;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.support.TestSecurityConfig;
import com.grown.smartoffice.support.WithMockAdminUser;
import com.grown.smartoffice.support.WithMockEmployeeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean UserService userService;
    @MockitoBean AccessLogService accessLogService;
    @MockitoBean UserPreferencesService userPreferencesService;

    // ── 전체 목록 (ADMIN) ─────────────────────────────────

    @Test
    @DisplayName("GET /users — 인증 없음 → 401")
    void list_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users — EMPLOYEE 권한 → 403")
    @WithMockEmployeeUser
    void list_asEmployee_403() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /users — ADMIN 권한 → 200, 페이지네이션 파라미터가 서비스에 전달")
    @WithMockAdminUser
    void list_asAdmin_passesPageParams() throws Exception {
        UserListItemResponse item = UserListItemResponse.builder()
                .id(1L).name("홍길동").email("h@grown.com").role("USER")
                .department("개발팀").status("ACTIVE").hiredAt(LocalDate.of(2026, 3, 2)).build();
        PageResponse<UserListItemResponse> page = new PageResponse<>(List.of(item), 2, 5, 1L, 1, true);
        given(userService.getUsers(eq(3L), eq("ACTIVE"), eq("홍"), eq(2), eq(5))).willReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("departmentId", "3")
                        .param("status", "ACTIVE")
                        .param("keyword", "홍")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("h@grown.com"))
                .andExpect(jsonPath("$.data.page").value(2));
    }

    // ── 직원 등록 (ADMIN) ──────────────────────────────────

    @Test
    @DisplayName("POST /users — ADMIN 권한 → 201")
    @WithMockAdminUser
    void create_asAdmin_201() throws Exception {
        given(userService.createUser(any())).willReturn(UserCreateResponse.builder()
                .id(10L).employeeNumber("EMP010").name("김신입").email("newbie@grown.com")
                .role("USER").status("ACTIVE").hiredAt(LocalDate.of(2026, 3, 2)).build());

        String body = objectMapper.writeValueAsString(Map.of(
                "employeeNumber", "EMP010",
                "name", "김신입",
                "email", "newbie@grown.com",
                "role", "USER",
                "position", "사원",
                "departmentId", 1,
                "phone", "010-0000-0000",
                "hiredAt", "2026-03-02"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    @DisplayName("POST /users — EMPLOYEE 권한 → 403")
    @WithMockEmployeeUser
    void create_asEmployee_403() throws Exception {
        String validBody = objectMapper.writeValueAsString(Map.of(
                "employeeNumber", "EMP777",
                "name", "테스트",
                "email", "t@grown.com",
                "role", "USER",
                "position", "사원",
                "departmentId", 1,
                "hiredAt", "2026-03-02"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isForbidden());
    }

    // ── /me ────────────────────────────────────────────────

    @Test
    @DisplayName("GET /users/me — 인증 없음 → 401")
    void me_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/me — EMPLOYEE 도 허용, JWT subject 로 이메일 전달")
    @WithMockEmployeeUser(email = "me@grown.com")
    void me_asEmployee_usesSubjectEmail() throws Exception {
        given(userService.getMyInfo("me@grown.com")).willReturn(UserMeInfoResponse.builder()
                .id(7L).email("me@grown.com").role("USER").status("ACTIVE").build());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@grown.com"));

        verify(userService).getMyInfo("me@grown.com");
    }

    @Test
    @DisplayName("POST /users/me — 비밀번호 변경 요청")
    @WithMockEmployeeUser(email = "me@grown.com")
    void updateMe_ok() throws Exception {
        given(userService.updateMyInfo(eq("me@grown.com"), any())).willReturn(
                UserMeUpdateResponse.builder().phone("010-1111-2222").build());

        String body = objectMapper.writeValueAsString(Map.of(
                "phone", "010-1111-2222",
                "currentPassword", "oldPassword1!",
                "password", "newPassword1!"));

        mockMvc.perform(post("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("010-1111-2222"));
    }

    // ── /me/preferences ────────────────────────────────────

    @Test
    @DisplayName("GET /users/me/preferences — EMPLOYEE 도 허용, JWT subject 로 이메일 전달")
    @WithMockEmployeeUser(email = "me@grown.com")
    void getMyPreferences_asEmployee_200() throws Exception {
        given(userPreferencesService.getMyPreferences("me@grown.com")).willReturn(
                UserPreferencesResponse.builder()
                        .userId(7L).notificationsEnabled(true).language("ko").theme("light").build());

        mockMvc.perform(get("/api/v1/users/me/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.language").value("ko"));

        verify(userPreferencesService).getMyPreferences("me@grown.com");
    }

    @Test
    @DisplayName("PUT /users/me/preferences — 부분 수정 요청")
    @WithMockEmployeeUser(email = "me@grown.com")
    void updateMyPreferences_ok() throws Exception {
        given(userPreferencesService.updateMyPreferences(eq("me@grown.com"), any())).willReturn(
                UserPreferencesResponse.builder()
                        .userId(7L).notificationsEnabled(false).language("en").theme("dark").build());

        String body = objectMapper.writeValueAsString(Map.of(
                "notificationsEnabled", false, "language", "en", "theme", "dark"));

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.theme").value("dark"));
    }

    @Test
    @DisplayName("GET /users/me/preferences — 인증 없음 → 401")
    void getMyPreferences_noAuth_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/preferences"))
                .andExpect(status().isUnauthorized());
    }

    // ── 개별 (ADMIN) ───────────────────────────────────────

    @Test
    @DisplayName("GET /users/{id} — ADMIN 권한 → 200")
    @WithMockAdminUser
    void detail_asAdmin_200() throws Exception {
        given(userService.getUserDetail(1L)).willReturn(UserDetailResponse.builder()
                .id(1L).name("관리자").email("admin@grown.com").role("ADMIN").status("ACTIVE").build());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@grown.com"));
    }

    @Test
    @DisplayName("PUT /users/{id} — EMPLOYEE 권한 → 403")
    @WithMockEmployeeUser
    void update_asEmployee_403() throws Exception {
        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /users/{id} — ADMIN 권한 → 200")
    @WithMockAdminUser
    void deactivate_asAdmin_200() throws Exception {
        mockMvc.perform(delete("/api/v1/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));

        verify(userService).deactivateUser(5L);
    }

    @Test
    @DisplayName("GET /users/{id}/access-logs — ADMIN 권한으로 200")
    @WithMockAdminUser
    void getUserAccessLogs_admin_200() throws Exception {
        given(accessLogService.getUserAccessLogs(anyLong(), any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(UserAccessLogListResponse.of(1L, "박성종", new PageResponse<>(Collections.emptyList(), 0, 20, 0, 0, true)));

        mockMvc.perform(get("/api/v1/users/1/access-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"))
                .andExpect(jsonPath("$.data.userName").value("박성종"));
    }

    @Test
    @DisplayName("GET /users/{id}/access-logs — EMPLOYEE 호출 시 403")
    @WithMockEmployeeUser
    void getUserAccessLogs_employee_403() throws Exception {
        mockMvc.perform(get("/api/v1/users/1/access-logs"))
                .andExpect(status().isForbidden());
    }
}
