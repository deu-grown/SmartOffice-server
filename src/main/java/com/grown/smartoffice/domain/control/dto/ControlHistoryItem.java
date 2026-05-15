package com.grown.smartoffice.domain.control.dto;

import com.grown.smartoffice.domain.control.entity.ControlCommand;
import com.grown.smartoffice.domain.control.entity.ControlCommandType;
import com.grown.smartoffice.domain.control.entity.ControlStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ControlHistoryItem {
    private Long id;
    private Long deviceId;
    private ControlCommandType command;
    private ControlStatus status;
    private LocalDateTime requestTime;

    public ControlHistoryItem(ControlCommand command) {
        this.id = command.getId();
        this.deviceId = command.getDevice().getDevicesId();
        this.command = command.getCommandType();
        this.status = command.getStatus();
        this.requestTime = command.getTriggeredAt();
    }
}
