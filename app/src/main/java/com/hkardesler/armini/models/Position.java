/*
 * *
 *  * Created by Haydar Kardesler on 2.06.2022 20:17
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public class Position {
    private long key;
    private MotorSpeed motorSpeed;
    private long base, shoulder, elbowVertical, elbowHorizontal, wristVertical, wristHorizontal, gripper;

    public Position() {
    }

    public Position(long key, MotorSpeed motorSpeed, long baseValue, long shoulderValue, long elbowVerticalValue, long elbowHorizontalValue, long wristVerticalValue, long wristHorizontalValue, long gripperValue) {
        this.key = key;
        this.motorSpeed = motorSpeed;
        this.base = baseValue;
        this.shoulder = shoulderValue;
        this.elbowVertical = elbowVerticalValue;
        this.elbowHorizontal = elbowHorizontalValue;
        this.wristVertical = wristVerticalValue;
        this.wristHorizontal = wristHorizontalValue;
        gripper = gripperValue;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public MotorSpeed getMotorSpeed() {
        return motorSpeed;
    }

    public void setMotorSpeed(MotorSpeed motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

    public long getBase() {
        return base;
    }

    public void setBase(long base) {
        this.base = base;
    }

    public long getShoulder() {
        return shoulder;
    }

    public void setShoulder(long shoulder) {
        this.shoulder = shoulder;
    }

    public long getElbowVertical() {
        return elbowVertical;
    }

    public void setElbowVertical(long elbowVertical) {
        this.elbowVertical = elbowVertical;
    }

    public long getElbowHorizontal() {
        return elbowHorizontal;
    }

    public void setElbowHorizontal(long elbowHorizontal) {
        this.elbowHorizontal = elbowHorizontal;
    }

    public long getWristVertical() {
        return wristVertical;
    }

    public void setWristVertical(long wristVertical) {
        this.wristVertical = wristVertical;
    }

    public long getWristHorizontal() {
        return wristHorizontal;
    }

    public void setWristHorizontal(long wristHorizontal) {
        this.wristHorizontal = wristHorizontal;
    }

    public long getGripper() {
        return gripper;
    }

    public void setGripper(long gripper) {
        this.gripper = gripper;
    }
}
