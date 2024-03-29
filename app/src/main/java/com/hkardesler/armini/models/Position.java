/*
 * *
 *  * Created by Alper Kardesler on 2.06.2022 20:17
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public class Position {
    private int key;
    private MotorSpeedEnum motorSpeed;
    private int waitingTime, base, shoulder, elbowVertical, elbowHorizontal, wristVertical, wristHorizontal, gripper;

    public Position() {
    }

    public Position(int key, MotorSpeedEnum motorSpeed, int waitingTime, int baseValue, int shoulderValue, int elbowVerticalValue, int elbowHorizontalValue, int wristVerticalValue, int wristHorizontalValue, int gripperValue) {
        this.key = key;
        this.motorSpeed = motorSpeed;
        this.waitingTime = waitingTime;
        this.base = baseValue;
        this.shoulder = shoulderValue;
        this.elbowVertical = elbowVerticalValue;
        this.elbowHorizontal = elbowHorizontalValue;
        this.wristVertical = wristVerticalValue;
        this.wristHorizontal = wristHorizontalValue;
        gripper = gripperValue;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public MotorSpeedEnum getMotorSpeed() {
        return motorSpeed;
    }

    public void setMotorSpeed(MotorSpeedEnum motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getShoulder() {
        return shoulder;
    }

    public void setShoulder(int shoulder) {
        this.shoulder = shoulder;
    }

    public int getElbowVertical() {
        return elbowVertical;
    }

    public void setElbowVertical(int elbowVertical) {
        this.elbowVertical = elbowVertical;
    }

    public int getElbowHorizontal() {
        return elbowHorizontal;
    }

    public void setElbowHorizontal(int elbowHorizontal) {
        this.elbowHorizontal = elbowHorizontal;
    }

    public int getWristVertical() {
        return wristVertical;
    }

    public void setWristVertical(int wristVertical) {
        this.wristVertical = wristVertical;
    }

    public int getWristHorizontal() {
        return wristHorizontal;
    }

    public void setWristHorizontal(int wristHorizontal) {
        this.wristHorizontal = wristHorizontal;
    }

    public int getGripper() {
        return gripper;
    }

    public void setGripper(int gripper) {
        this.gripper = gripper;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }
}
