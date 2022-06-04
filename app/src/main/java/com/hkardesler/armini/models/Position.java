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
    private String baseValue, shoulderValue, elbowVerticalValue, elbowHorizontalValue, wristVerticalValue, wristHorizontalValue, GripperValue;

    public Position() {
    }

    public Position(String id, MotorSpeed motorSpeed, String baseValue, String shoulderValue, String elbowVerticalValue, String elbowHorizontalValue, String wristVerticalValue, String wristHorizontalValue, String gripperValue) {
        this.key = id;
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

    public String getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(String baseValue) {
        this.baseValue = baseValue;
    }

    public String getShoulderValue() {
        return shoulderValue;
    }

    public void setShoulderValue(String shoulderValue) {
        this.shoulderValue = shoulderValue;
    }

    public String getElbowVerticalValue() {
        return elbowVerticalValue;
    }

    public void setElbowVerticalValue(String elbowVerticalValue) {
        this.elbowVerticalValue = elbowVerticalValue;
    }

    public String getElbowHorizontalValue() {
        return elbowHorizontalValue;
    }

    public void setElbowHorizontalValue(String elbowHorizontalValue) {
        this.elbowHorizontalValue = elbowHorizontalValue;
    }

    public String getWristVerticalValue() {
        return wristVerticalValue;
    }

    public void setWristVerticalValue(String wristVerticalValue) {
        this.wristVerticalValue = wristVerticalValue;
    }

    public String getWristHorizontalValue() {
        return wristHorizontalValue;
    }

    public void setWristHorizontalValue(String wristHorizontalValue) {
        this.wristHorizontalValue = wristHorizontalValue;
    }

    public String getGripperValue() {
        return GripperValue;
    }

    public void setGripperValue(String gripperValue) {
        GripperValue = gripperValue;
    }
}
