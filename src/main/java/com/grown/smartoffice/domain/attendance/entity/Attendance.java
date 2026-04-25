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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "work_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "work_minutes")
    private Integer workMinutes;

    @Column(name = "overtime_minutes")
    private Integer overtimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 15)
    private AttendanceStatus attendanceStatus;

    @Column(name = "attendance_note", length = 255)
    private String attendanceNote;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Attendance(User user, LocalDate workDate, LocalDateTime checkIn,
                      AttendanceStatus attendanceStatus) {
        this.user = user;
        this.workDate = workDate;
        this.checkIn = checkIn;
        this.attendanceStatus = attendanceStatus != null ? attendanceStatus : AttendanceStatus.NORMAL;
        this.overtimeMinutes = 0;
    }

    public void recordCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }

    public void applyBatchResult(int workMinutes, int overtimeMinutes, AttendanceStatus status, String note) {
        this.workMinutes = workMinutes;
        this.overtimeMinutes = overtimeMinutes;
        this.attendanceStatus = status;
        this.attendanceNote = note;
    }

    public void correct(LocalDateTime checkIn, LocalDateTime checkOut, String note) {
        if (checkIn != null) this.checkIn = checkIn;
        if (checkOut != null) this.checkOut = checkOut;
        if (note != null) this.attendanceNote = note;
    }
}
