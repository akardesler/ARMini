/*
 * *
 *  * Created by Haydar Kardesler on 2.06.2022 20:17
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

import java.util.ArrayList;

public class Scenario {

   private String id, name;
   private int positionCount;
   private WorkingMode workingMode;
    private int loopCount;
    private boolean isLightOpen;

    public Scenario(String id, String name, int positionCount, WorkingMode workingMode, int loopCount, boolean isLightOpen) {
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

    public WorkingMode getWorkingMode() {
        return workingMode;
    }

    public void setWorkingMode(WorkingMode workingMode) {
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
