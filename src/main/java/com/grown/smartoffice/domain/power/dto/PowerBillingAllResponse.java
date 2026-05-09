package com.grown.smartoffice.domain.power.dto;

import com.grown.smartoffice.domain.power.entity.PowerBilling;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class PowerBillingAllResponse {

    private Integer year;
    private Integer month;
    private BigDecimal totalKwh;
    private Integer totalFee;
    private List<ZoneSummary> zones;

    @Getter
    @Builder
    public static class ZoneSummary {
        private Long zoneId;
        private String zoneName;
        private BigDecimal totalKwh;
        private Integer totalFee;

        public static ZoneSummary from(PowerBilling pb) {
            return ZoneSummary.builder()
                    .zoneId(pb.getZone().getZoneId())
                    .zoneName(pb.getZone().getZoneName())
                    .totalKwh(pb.getTotalKwh())
                    .totalFee(pb.getTotalFee())
                    .build();
        }
    }

    public static PowerBillingAllResponse from(int year, int month, List<PowerBilling> records) {
        BigDecimal totalKwh = records.stream()
                .map(PowerBilling::getTotalKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalFee = records.stream()
                .mapToInt(PowerBilling::getTotalFee)
                .sum();
        List<ZoneSummary> zones = records.stream()
                .map(ZoneSummary::from)
                .toList();
        return PowerBillingAllResponse.builder()
                .year(year)
                .month(month)
                .totalKwh(totalKwh)
                .totalFee(totalFee)
                .zones(zones)
                .build();
    }
}
