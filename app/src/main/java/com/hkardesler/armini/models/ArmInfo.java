/*
 * *
 *  * Created by Alper Kardesler on 10.06.2022 07:45
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.models;

import com.hkardesler.armini.impls.ArminiStatusChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ArmInfo {
    private static ArminiStatusEnum status;
    private static String operatorId = "";
    private static String scenarioId = "";
    private static final List<ArminiStatusChangeListener> listeners = new ArrayList<ArminiStatusChangeListener>();

    public static ArminiStatusEnum getStatus() { return status; }

    public static void setStatus(ArminiStatusEnum value) {
        status = value;

        for (ArminiStatusChangeListener l : listeners) {
            l.OnArminiStatusChanged(status);
        }
    }

    public static String getOperatorId() {
        return operatorId;
    }

    public static void setOperatorId(String operatorId) {
        ArmInfo.operatorId = operatorId;
    }

    public static String getScenarioId() {
        return scenarioId;
    }

    public static void setScenarioId(String scenarioId) {
        ArmInfo.scenarioId = scenarioId;
    }

    public static void addArminiStatusChangedListener(ArminiStatusChangeListener l) {
        listeners.add(l);
    }


}