/*
 * *
 *  * Created by Alper Kardesler on 5.06.2022 22:41
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public enum ControllerModeEnum {
    JOYSTICK("Joystick", 0),
    SLIDER("Slider", 1);

    private final String stringValue;
    private final int intValue;

    ControllerModeEnum(String toString, int value) {
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
