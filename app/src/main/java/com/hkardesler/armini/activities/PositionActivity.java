/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 03:05
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityMainBinding;
import com.hkardesler.armini.databinding.ActivityPositionBinding;
import com.hkardesler.armini.fragments.JoystickFragment;
import com.hkardesler.armini.fragments.SliderFragment;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.models.ControllerMode;
import com.hkardesler.armini.models.MotorSpeed;
import com.hkardesler.armini.models.Scenario;

import java.lang.reflect.Type;

public class PositionActivity extends BaseActivity {

    ActivityPositionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPositionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        int controllerModeInt = prefs.getInt(Global.CONTROLLER_MODE_POSITION_KEY, Global.CONTROLLER_MODE_POSITION_VALUE.getIntValue());
        if(controllerModeInt == ControllerMode.JOYSTICK.getIntValue()){
            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerMode.JOYSTICK;
        }else{
            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerMode.SLIDER;
        }

        String jsonPosition = getIntent().getExtras().getString(Global.POSITION_KEY);
        boolean isNewPosition = getIntent().getExtras().getBoolean(Global.NEW_POSITION_KEY);
        String scenarioId = getIntent().getExtras().getString(Global.SCENARIO_ID_KEY);

        if (savedInstanceState == null) {
          /*
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SliderFragment.newInstance(jsonPosition, isNewPosition, scenarioId))
                    .commitNow();*/
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, JoystickFragment.newInstance())
                    .commitNow();
        }

    }

    @Override
    protected void setListeners() {

    }


}