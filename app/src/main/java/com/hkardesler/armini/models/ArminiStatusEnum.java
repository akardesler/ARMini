/*
 * *
 *  * Created by Alper Kardesler on 2.06.2022 20:27
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public enum ArminiStatusEnum {
    OFFLINE("Offline", 0),
    AVAILABLE("Available", 1),
    BUSY("Busy", 2);

    private final String stringValue;
    private final int intValue;

    ArminiStatusEnum(String toString, int value) {
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
