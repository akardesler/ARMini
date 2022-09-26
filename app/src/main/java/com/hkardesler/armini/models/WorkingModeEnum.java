/*
 * *
 *  * Created by Alper Kardesler on 2.06.2022 20:29
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public enum WorkingModeEnum {
    INFINITE("Infinite", 0),
    LOOP("Loop", 1);
    private final String stringValue;
    private final int intValue;

    WorkingModeEnum(String toString, int value) {
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
