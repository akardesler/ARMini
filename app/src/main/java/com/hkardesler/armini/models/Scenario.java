/*
 * *
 *  * Created by Alper Kardesler on 2.06.2022 20:17
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

public class Scenario {

   private String id, name;
   private int positionCount;
   private WorkingModeEnum workingMode;
    private int loopCount;
    private boolean isLightOpen;

    public Scenario(String id, String name, int positionCount, WorkingModeEnum workingMode, int loopCount, boolean isLightOpen) {
        this.id = id;
        this.name = name;
        this.positionCount = positionCount;
        this.workingMode = workingMode;
        this.isLightOpen = isLightOpen;
        this.loopCount = loopCount;
    }

    public Scenario() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkingModeEnum getWorkingMode() {
        return workingMode;
    }

    public void setWorkingMode(WorkingModeEnum workingMode) {
        this.workingMode = workingMode;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public boolean isLightOpen() {
        return isLightOpen;
    }

    public void setLightOpen(boolean lightOpen) {
        isLightOpen = lightOpen;
    }

    public int getPositionCount() {
        return positionCount;
    }

    public void setPositionCount(int positionCount) {
        this.positionCount = positionCount;
    }
}
