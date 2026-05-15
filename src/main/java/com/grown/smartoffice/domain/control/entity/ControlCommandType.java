package com.grown.smartoffice.domain.control.entity;

/**
 * IoT 제어 명령 유형. control_commands.command_type 컬럼과 매핑.
 *
 * V8 시드 history 사용 값: AC · LIGHT · FAN.
 * SmartOffice-web ControlPanel.QUICK_COMMANDS 와 IoT(RPi) 처리 명령 정합.
 *
 * 신규 명령 추가 시: 본 enum + IoT 펌웨어 + web QUICK_COMMANDS 동시 갱신 필요.
 */
public enum ControlCommandType {
    AC,
    LIGHT,
    FAN,
    DOOR_LOCK,
    SET_TEMPERATURE
}
