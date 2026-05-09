package com.grown.smartoffice.domain.power.dto;

import com.grown.smartoffice.domain.power.entity.PowerBilling;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PowerBillingZoneResponse {

    private Long zoneId;
    private String zoneName;
    private List<BillingRecord> records;

    @Getter
    @Builder
    public static class BillingRecord {
        private Long id;
        private Integer year;
        private Integer month;
        private BigDecimal totalKwh;
        private Integer unitPrice;
        private Integer baseFee;
        private Integer usageFee;
        private Integer totalFee;
        private LocalDateTime createdAt;

        public static BillingRecord from(PowerBilling pb) {
            return BillingRecord.builder()
                    .id(pb.getBillingId())
                    .year(pb.getBillingYear())
                    .month(pb.getBillingMonth())
                    .totalKwh(pb.getTotalKwh())
                    .unitPrice(pb.getUnitPrice())
                    .baseFee(pb.getBaseFee())
                    .usageFee(pb.getPowerFee())
                    .totalFee(pb.getTotalFee())
                    .createdAt(pb.getCalculatedAt())
                    .build();
        }
    }

    public static PowerBillingZoneResponse from(Long zoneId, String zoneName, List<PowerBilling> records) {
        return PowerBillingZoneResponse.builder()
                .zoneId(zoneId)
                .zoneName(zoneName)
                .records(records.stream().map(BillingRecord::from).toList())
                .build();
    }
}
