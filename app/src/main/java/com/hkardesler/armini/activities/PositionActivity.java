/*
 * *
 *  * Created by Alper Kardesler on 5.06.2022 03:05
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityPositionBinding;
import com.hkardesler.armini.fragments.JoystickControlFragment;
import com.hkardesler.armini.fragments.SliderControlFragment;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.impls.ArminiStatusChangeListener;
import com.hkardesler.armini.impls.ControllerModeChangeListener;
import com.hkardesler.armini.models.ArmInfo;
import com.hkardesler.armini.models.ArminiStatusEnum;
import com.hkardesler.armini.models.ControllerModeEnum;

public class PositionActivity extends BaseActivity implements ControllerModeChangeListener {

    ActivityPositionBinding binding;
    String jsonPosition;
    boolean isNewPosition;
    String scenarioId;
    SliderControlFragment fragmentSlider;
    JoystickControlFragment fragmentJoystick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPositionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int controllerModeInt = prefs.getInt(Global.CONTROLLER_MODE_POSITION_KEY, Global.CONTROLLER_MODE_POSITION_VALUE.getIntValue());
        if(controllerModeInt == ControllerModeEnum.JOYSTICK.getIntValue()){
            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.JOYSTICK;
        }else{
            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.SLIDER;
        }

        jsonPosition = getIntent().getExtras().getString(Global.POSITION_KEY);
        isNewPosition = getIntent().getExtras().getBoolean(Global.NEW_POSITION_KEY);
        scenarioId = getIntent().getExtras().getString(Global.SCENARIO_ID_KEY);
        fragmentJoystick = JoystickControlFragment.newInstance(jsonPosition, isNewPosition, scenarioId, this);
        fragmentSlider = SliderControlFragment.newInstance(jsonPosition, isNewPosition, scenarioId, this);

        if (savedInstanceState == null) {

            if(Global.CONTROLLER_MODE_POSITION_VALUE == ControllerModeEnum.JOYSTICK){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentJoystick)
                        .commitNow();

            }else{
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentSlider)
                        .commitNow();
            }

        }

    }

    @Override
    protected void setListeners() {
        ArmInfo.addArminiStatusChangedListener(new ArminiStatusChangeListener() {
            @Override
            public void OnArminiStatusChanged(ArminiStatusEnum status) {
                if(status == ArminiStatusEnum.OFFLINE)
                    finish();
            }
        });
    }


    @Override
    public void onControllerModeChanged() {
        if(Global.CONTROLLER_MODE_POSITION_VALUE == ControllerModeEnum.JOYSTICK){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragmentJoystick)
                    .addToBackStack(null)
                    .commit();
        }else if(Global.CONTROLLER_MODE_POSITION_VALUE == ControllerModeEnum.SLIDER){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragmentSlider)
                    .addToBackStack(null)
                    .commit();

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE) {
            AppUtils.updateInfoOnFirebase(ArminiStatusEnum.AVAILABLE);
        }
    }
}