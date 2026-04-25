package com.grown.smartoffice.domain.salary.entity;

import com.grown.smartoffice.domain.attendance.entity.MonthlyAttendance;
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
@Table(name = "salary_records",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "salrec_year", "salrec_month"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SalaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salrec_id")
    private Long salrecId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monat_id", nullable = false)
    private MonthlyAttendance monthlyAttendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salset_id", nullable = false)
    private SalarySetting salarySetting;

    @Column(name = "salrec_year", nullable = false)
    private int salrecYear;

    @Column(name = "salrec_month", nullable = false)
    private int salrecMonth;

    @Column(name = "salrec_base_salary", nullable = false)
    private int salrecBaseSalary;

    @Column(name = "overtime_pay")
    private int overtimePay;

    @Column(name = "total_pay", nullable = false)
    private int totalPay;

    @Enumerated(EnumType.STRING)
    @Column(name = "salrec_status", nullable = false, length = 10)
    private SalaryStatus salrecStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public SalaryRecord(User user, MonthlyAttendance monthlyAttendance, SalarySetting salarySetting,
                        int salrecYear, int salrecMonth, int salrecBaseSalary, int overtimePay, int totalPay) {
        this.user = user;
        this.monthlyAttendance = monthlyAttendance;
        this.salarySetting = salarySetting;
        this.salrecYear = salrecYear;
        this.salrecMonth = salrecMonth;
        this.salrecBaseSalary = salrecBaseSalary;
        this.overtimePay = overtimePay;
        this.totalPay = totalPay;
        this.salrecStatus = SalaryStatus.DRAFT;
    }

    public void overwrite(MonthlyAttendance monat, SalarySetting setting,
                          int baseSalary, int overtimePay, int totalPay) {
        this.monthlyAttendance = monat;
        this.salarySetting = setting;
        this.salrecBaseSalary = baseSalary;
        this.overtimePay = overtimePay;
        this.totalPay = totalPay;
        this.salrecStatus = SalaryStatus.DRAFT;
    }

    public void confirm() {
        this.salrecStatus = SalaryStatus.CONFIRMED;
    }
}
