/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 06:10
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityPositionBinding;
import com.hkardesler.armini.databinding.ActivityPositionSettingsBinding;
import com.hkardesler.armini.databinding.ActivityScenarioSettingsBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.models.ControllerMode;
import com.hkardesler.armini.models.MotorSpeed;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.Scenario;
import com.hkardesler.armini.models.User;
import com.hkardesler.armini.models.WorkingMode;

import java.lang.reflect.Type;

public class PositionSettingsActivity extends BaseActivity {
    ActivityPositionSettingsBinding binding;
    Position position;
    int deletePositionId = -1;
    boolean isNewPosition = true;
    boolean controllerModeChanged = false;
    String scenarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPositionSettingsBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        String json = getIntent().getExtras().getString(Global.POSITION_KEY);
        Type type = new TypeToken<Position>() {}.getType();
        position = gson.fromJson(json, type);
        isNewPosition = getIntent().getExtras().getBoolean(Global.NEW_POSITION_KEY);

        setMotorSpeed(position.getMotorSpeed());
        setControllerMode(Global.CONTROLLER_MODE_POSITION_VALUE);
        setListeners();

        if(isNewPosition){
            binding.btnDeletePosition.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setListeners() {

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
            }
        });
        binding.btnDeletePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showDeleteDialog();
            }
        });

        binding.cardSlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMotorSpeed(MotorSpeed.SLOW);
            }
        });

        binding.cardNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMotorSpeed(MotorSpeed.NORMAL);
            }
        });

        binding.cardFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMotorSpeed(MotorSpeed.FAST);
            }
        });

        binding.cardJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setControllerMode(ControllerMode.JOYSTICK);
                Global.CONTROLLER_MODE_POSITION_VALUE = ControllerMode.JOYSTICK;
                AppUtils.putInt(PositionSettingsActivity.this, Global.CONTROLLER_MODE_POSITION_KEY, ControllerMode.JOYSTICK.getIntValue());
                controllerModeChanged = true;

            }
        });

        binding.cardSlider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setControllerMode(ControllerMode.SLIDER);
                Global.CONTROLLER_MODE_POSITION_VALUE = ControllerMode.SLIDER;
                AppUtils.putInt(PositionSettingsActivity.this, Global.CONTROLLER_MODE_POSITION_KEY, ControllerMode.SLIDER.getIntValue());
                controllerModeChanged = true;
            }
        });

    }


    private void setControllerMode(ControllerMode mode){
        if(mode == ControllerMode.JOYSTICK){
            binding.lnJoystick.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgJoystick.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtJoystick.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnSlider.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgSlider.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtSlider.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerMode.JOYSTICK;

        }else{
            binding.lnSlider.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgSlider.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtSlider.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnJoystick.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgJoystick.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtJoystick.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerMode.SLIDER;
        }
    }

    private void setMotorSpeed(MotorSpeed speed){
        if(speed == MotorSpeed.SLOW){
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeed.SLOW;
            AppUtils.putInt(PositionSettingsActivity.this, Global.MOTOR_SPEED_POSITION_KEY, MotorSpeed.SLOW.getIntValue());
            position.setMotorSpeed(MotorSpeed.SLOW);
        }else if(speed == MotorSpeed.NORMAL){
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeed.NORMAL;
            AppUtils.putInt(PositionSettingsActivity.this, Global.MOTOR_SPEED_POSITION_KEY, MotorSpeed.NORMAL.getIntValue());
            position.setMotorSpeed(MotorSpeed.NORMAL);
        }else{
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.white));

            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeed.FAST;
            AppUtils.putInt(PositionSettingsActivity.this, Global.MOTOR_SPEED_POSITION_KEY, MotorSpeed.FAST.getIntValue());
            position.setMotorSpeed(MotorSpeed.FAST);
        }
    }

    private void finishActivity(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Global.POSITION_KEY, gson.toJson(position));
        resultIntent.putExtra(Global.DELETE_POSITION_ID, deletePositionId);
        resultIntent.putExtra(Global.CONTROLLER_MODE_CHANGED, controllerModeChanged);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void showDeleteDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_position_title))
                .setMessage(getString(R.string.delete_position_desc))

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deletePositionId = position.getKey();
                        finishActivity();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }


}