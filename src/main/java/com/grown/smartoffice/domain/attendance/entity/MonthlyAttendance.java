package com.grown.smartoffice.domain.attendance.entity;

import com.grown.smartoffice.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_attendance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "monat_year", "monat_month"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MonthlyAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monat_id")
    private Long monatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "monat_year", nullable = false)
    private int monatYear;

    @Column(name = "monat_month", nullable = false)
    private int monatMonth;

    @Column(name = "monat_total_work_minutes")
    private int monatTotalWorkMinutes;

    @Column(name = "monat_overtime_minutes")
    private int monatOvertimeMinutes;

    @Column(name = "late_count")
    private int lateCount;

    @Column(name = "early_leave_count")
    private int earlyLeaveCount;

    @Column(name = "absent_count")
    private int absentCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MonthlyAttendance(User user, int monatYear, int monatMonth,
                             int monatTotalWorkMinutes, int monatOvertimeMinutes,
                             int lateCount, int earlyLeaveCount, int absentCount) {
        this.user = user;
        this.monatYear = monatYear;
        this.monatMonth = monatMonth;
        this.monatTotalWorkMinutes = monatTotalWorkMinutes;
        this.monatOvertimeMinutes = monatOvertimeMinutes;
        this.lateCount = lateCount;
        this.earlyLeaveCount = earlyLeaveCount;
        this.absentCount = absentCount;
    }

    public void update(int totalWorkMinutes, int overtimeMinutes,
                       int lateCount, int earlyLeaveCount, int absentCount) {
        this.monatTotalWorkMinutes = totalWorkMinutes;
        this.monatOvertimeMinutes = overtimeMinutes;
        this.lateCount = lateCount;
        this.earlyLeaveCount = earlyLeaveCount;
        this.absentCount = absentCount;
    }
}
