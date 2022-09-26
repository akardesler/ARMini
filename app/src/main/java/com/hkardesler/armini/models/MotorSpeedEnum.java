/*
 * *
 *  * Created by Alper Kardesler on 2.06.2022 20:21
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public enum MotorSpeedEnum {
    SLOW("Slow", 8),
    NORMAL("Normal", 6),
    FAST("Fast", 4);

    private final String stringValue;
    private final int intValue;

    MotorSpeedEnum(String toString, int value) {
        stringValue = toString;
        intValue = value;
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
