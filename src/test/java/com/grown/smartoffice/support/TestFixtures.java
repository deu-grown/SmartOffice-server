package com.grown.smartoffice.support;

import com.grown.smartoffice.domain.department.entity.Department;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserRole;
import com.grown.smartoffice.domain.user.entity.UserStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 테스트용 픽스처 빌더.
 * 엔티티의 ID/감사 필드는 @Builder로 설정 불가 → ReflectionTestUtils로 주입.
 */
public final class TestFixtures {

    private TestFixtures() {}

    public static Department department(Long id, String name) {
        Department d = Department.builder()
                .deptName(name)
                .deptDescription(name + " 설명")
                .build();
        setAuditFields(d, id, "deptId");
        return d;
    }

    public static User adminUser(Long id, String email, Department dept) {
        return user(id, email, UserRole.ADMIN, UserStatus.ACTIVE, dept);
    }

    public static User employeeUser(Long id, String email, Department dept) {
        return user(id, email, UserRole.USER, UserStatus.ACTIVE, dept);
    }

    public static User inactiveUser(Long id, String email, Department dept) {
        return user(id, email, UserRole.USER, UserStatus.INACTIVE, dept);
    }

    public static User user(Long id, String email, UserRole role, UserStatus status, Department dept) {
        User u = User.builder()
                .department(dept)
                .employeeNumber(String.format("EMP%03d", id))
                .employeeName("직원" + id)
                .employeeEmail(email)
                .password("$2a$10$encodedPasswordPlaceholder")
                .role(role)
                .position("사원")
                .phone("010-0000-0000")
                .status(status)
                .hiredAt(LocalDate.of(2026, 3, 2))
                .build();
        setAuditFields(u, id, "userId");
        return u;
    }

    private static void setAuditFields(Object entity, Long id, String idFieldName) {
        LocalDateTime now = LocalDateTime.now();
        ReflectionTestUtils.setField(entity, idFieldName, id);
        ReflectionTestUtils.setField(entity, "createdAt", now);
        ReflectionTestUtils.setField(entity, "updatedAt", now);
    }
}
