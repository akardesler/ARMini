/*
 * *
 *  * Created by Haydar Kardesler on 2.06.2022 20:17
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public class Position {
    private String key;
    private MotorSpeed motorSpeed;
    private long baseValue, shoulderValue, elbowVerticalValue, elbowHorizontalValue, wristVerticalValue, wristHorizontalValue, GripperValue;

    public Position() {
    }

    public Position(String key, MotorSpeed motorSpeed, long baseValue, long shoulderValue, long elbowVerticalValue, long elbowHorizontalValue, long wristVerticalValue, long wristHorizontalValue, long gripperValue) {
        this.key = key;
        this.motorSpeed = motorSpeed;
        this.baseValue = baseValue;
        this.shoulderValue = shoulderValue;
        this.elbowVerticalValue = elbowVerticalValue;
        this.elbowHorizontalValue = elbowHorizontalValue;
        this.wristVerticalValue = wristVerticalValue;
        this.wristHorizontalValue = wristHorizontalValue;
        GripperValue = gripperValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MotorSpeed getMotorSpeed() {
        return motorSpeed;
    }

    public void setMotorSpeed(MotorSpeed motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

    public long getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(long baseValue) {
        this.baseValue = baseValue;
    }

    public long getShoulderValue() {
        return shoulderValue;
    }

    public void setShoulderValue(long shoulderValue) {
        this.shoulderValue = shoulderValue;
    }

    public long getElbowVerticalValue() {
        return elbowVerticalValue;
    }

    public void setElbowVerticalValue(long elbowVerticalValue) {
        this.elbowVerticalValue = elbowVerticalValue;
    }

    public long getElbowHorizontalValue() {
        return elbowHorizontalValue;
    }

    public void setElbowHorizontalValue(long elbowHorizontalValue) {
        this.elbowHorizontalValue = elbowHorizontalValue;
    }

    public long getWristVerticalValue() {
        return wristVerticalValue;
    }

    public void setWristVerticalValue(long wristVerticalValue) {
        this.wristVerticalValue = wristVerticalValue;
    }

    public long getWristHorizontalValue() {
        return wristHorizontalValue;
    }

    public void setWristHorizontalValue(long wristHorizontalValue) {
        this.wristHorizontalValue = wristHorizontalValue;
    }

    public long getGripperValue() {
        return GripperValue;
    }

    public void setGripperValue(long gripperValue) {
        GripperValue = gripperValue;
    }
}
