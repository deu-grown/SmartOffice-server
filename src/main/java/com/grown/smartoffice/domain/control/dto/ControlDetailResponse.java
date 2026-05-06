package com.grown.smartoffice.domain.control.dto;

import com.grown.smartoffice.domain.control.entity.ControlCommand;
import com.grown.smartoffice.domain.control.entity.ControlStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ControlDetailResponse {
    private Long controlId;
    private ControlStatus status;
    private LocalDateTime requestTime;
    private String resultMessage;

    public ControlDetailResponse(ControlCommand command) {
        this.controlId = command.getId();
        this.status = command.getStatus();
        this.requestTime = command.getTriggeredAt();
        this.resultMessage = command.getPayload(); // Simplified for now
    }
}
