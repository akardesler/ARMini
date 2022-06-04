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
   private ArrayList<Position> positions;
   private WorkingMode workingMode;
    private long loopCount;
    private boolean isLightOpen;

    public Scenario(String id, String name, ArrayList<Position> positions, WorkingMode workingMode, long loopCount, boolean isLightOpen) {
        this.id = id;
        this.name = name;
        this.positions = positions;
        this.workingMode = workingMode;
        this.isLightOpen = isLightOpen;
        this.loopCount = loopCount;
    }

    public Scenario() {
        this.positions = new ArrayList<>();
    }

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

    public ArrayList<Position> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Position> positions) {
        this.positions = positions;
    }

    public WorkingMode getWorkingMode() {
        return workingMode;
    }

    public void setWorkingMode(WorkingMode workingMode) {
        this.workingMode = workingMode;
    }

    public long getLoopCount() {
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


}
