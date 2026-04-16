package com.grown.smartoffice.domain.user.entity;

import com.grown.smartoffice.domain.department.entity.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private Department department;

    @Column(name = "employee_number", nullable = false, unique = true, length = 20)
    private String employeeNumber;

    @Column(name = "employee_name", nullable = false, length = 50)
    private String employeeName;

    @Column(name = "employee_email", nullable = false, unique = true, length = 100)
    private String employeeEmail;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role;

    @Column(nullable = false, length = 50)
    private String position;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status;

    @Column(name = "hired_at", nullable = false)
    private LocalDate hiredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(Department department, String employeeNumber, String employeeName,
                String employeeEmail, String password, UserRole role, String position,
                String phone, UserStatus status, LocalDate hiredAt) {
        this.department = department;
        this.employeeNumber = employeeNumber;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.password = password;
        this.role = role;
        this.position = position;
        this.phone = phone;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
        this.hiredAt = hiredAt;
    }

    // ── 비즈니스 메서드 ─────────────────────────────────────

    public void updateByAdmin(String employeeName, UserRole role, String position,
                               Department department, String phone, LocalDate hiredAt) {
        if (employeeName != null) this.employeeName = employeeName;
        if (role != null)         this.role = role;
        if (position != null)     this.position = position;
        if (department != null)   this.department = department;
        if (phone != null)        this.phone = phone;
        if (hiredAt != null)      this.hiredAt = hiredAt;
    }

    public void updateByMe(String phone, String encodedPassword) {
        if (phone != null)           this.phone = phone;
        if (encodedPassword != null) this.password = encodedPassword;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    // ── UserDetails 구현 ──────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /** Spring Security 에서 식별자로 사용하는 값 = employee_email */
    @Override
    public String getUsername() {
        return employeeEmail;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
