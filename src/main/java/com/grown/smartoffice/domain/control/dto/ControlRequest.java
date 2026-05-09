package com.grown.smartoffice.domain.control.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ControlRequest {

    @NotNull(message = "구역 ID는 필수입니다.")
    private Long zoneId;

    @NotNull(message = "장치 ID는 필수입니다.")
    private Long deviceId;

    @NotBlank(message = "제어 명령은 필수입니다.")
    private String command;

    private String value;

    @Builder
    public ControlRequest(Long zoneId, Long deviceId, String command, String value) {
        this.zoneId = zoneId;
        this.deviceId = deviceId;
        this.command = command;
        this.value = value;
    }
}
