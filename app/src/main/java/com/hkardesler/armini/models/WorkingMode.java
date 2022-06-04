/*
 * *
 *  * Created by Haydar Kardesler on 2.06.2022 20:29
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public enum WorkingMode {
    INFINITE("INFINITE", 0),
    LOOP("LOOP", 1);
    private final String stringValue;
    private final int intValue;

    WorkingMode(String toString, int value) {
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
