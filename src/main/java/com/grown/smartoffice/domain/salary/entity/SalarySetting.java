package com.grown.smartoffice.domain.salary.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SalarySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salset_id")
    private Long salsetId;

    @Column(name = "salset_position", nullable = false, length = 50)
    private String salsetPosition;

    @Column(name = "base_salary", nullable = false)
    private int baseSalary;

    @Column(name = "overtime_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "night_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal nightRate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public SalarySetting(String salsetPosition, int baseSalary, BigDecimal overtimeRate,
                         BigDecimal nightRate, LocalDate effectiveFrom) {
        this.salsetPosition = salsetPosition;
        this.baseSalary = baseSalary;
        this.overtimeRate = overtimeRate;
        this.nightRate = nightRate;
        this.effectiveFrom = effectiveFrom;
    }

    public void closeAt(LocalDate date) {
        this.effectiveTo = date;
    }

    public void update(int baseSalary, BigDecimal overtimeRate, BigDecimal nightRate) {
        this.baseSalary = baseSalary;
        if (overtimeRate != null) this.overtimeRate = overtimeRate;
        if (nightRate != null) this.nightRate = nightRate;
    }

    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(effectiveFrom)
                && (effectiveTo == null || !today.isAfter(effectiveTo));
    }
}
