package com.grown.smartoffice.domain.power.entity;

import com.grown.smartoffice.domain.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "power_billing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PowerBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_id")
    private Long billingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "total_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalKwh;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "base_fee", nullable = false)
    private Integer baseFee;

    @Column(name = "power_fee", nullable = false)
    private Integer powerFee;

    @Column(name = "total_fee", nullable = false)
    private Integer totalFee;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Builder
    public PowerBilling(Zone zone, Integer billingYear, Integer billingMonth,
                        BigDecimal totalKwh, Integer unitPrice, Integer baseFee,
                        Integer powerFee, Integer totalFee, LocalDateTime calculatedAt) {
        this.zone = zone;
        this.billingYear = billingYear;
        this.billingMonth = billingMonth;
        this.totalKwh = totalKwh;
        this.unitPrice = unitPrice;
        this.baseFee = baseFee;
        this.powerFee = powerFee;
        this.totalFee = totalFee;
        this.calculatedAt = calculatedAt;
    }

    public void recalculate(BigDecimal totalKwh, Integer unitPrice, Integer baseFee,
                            Integer powerFee, Integer totalFee) {
        this.totalKwh = totalKwh;
        this.unitPrice = unitPrice;
        this.baseFee = baseFee;
        this.powerFee = powerFee;
        this.totalFee = totalFee;
        this.calculatedAt = LocalDateTime.now();
    }
}
