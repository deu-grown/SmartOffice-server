package com.grown.smartoffice.domain.attendance;

import java.time.LocalTime;

public final class AttendancePolicy {

    private AttendancePolicy() {}

    public static final LocalTime STANDARD_START = LocalTime.of(9, 0);
    public static final LocalTime STANDARD_END   = LocalTime.of(18, 0);
    public static final int LUNCH_BREAK_MINUTES  = 60;
    public static final int STANDARD_WORK_MINUTES = 480;
}
