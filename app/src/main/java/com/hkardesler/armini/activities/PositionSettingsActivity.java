/*
 * *
 *  * Created by Alper Kardesler on 5.06.2022 06:10
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityPositionSettingsBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.impls.ArminiStatusChangeListener;
import com.hkardesler.armini.models.ArmInfo;
import com.hkardesler.armini.models.ArminiStatusEnum;
import com.hkardesler.armini.models.ControllerModeEnum;
import com.hkardesler.armini.models.MotorSpeedEnum;
import com.hkardesler.armini.models.Position;

import java.lang.reflect.Type;

public class PositionSettingsActivity extends BaseActivity {
    ActivityPositionSettingsBinding binding;
    Position position;
    int deletePositionId = -1;
    boolean isNewPosition = true;
    boolean controllerModeChanged = false;

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
        binding.inputWaitingTime.setText(String.valueOf(position.getWaitingTime()));

        if(isNewPosition){
            binding.btnDeletePosition.setVisibility(View.GONE);
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
                setMotorSpeed(MotorSpeedEnum.SLOW);
            }
        });

        binding.cardNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMotorSpeed(MotorSpeedEnum.NORMAL);
            }
        });

        binding.cardFast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMotorSpeed(MotorSpeedEnum.FAST);
            }
        });

        binding.cardJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setControllerMode(ControllerModeEnum.JOYSTICK);
                Global.CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.JOYSTICK;
                AppUtils.putInt(PositionSettingsActivity.this, Global.CONTROLLER_MODE_POSITION_KEY, ControllerModeEnum.JOYSTICK.getIntValue());
                controllerModeChanged = true;

            }
        });

        binding.cardSlider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setControllerMode(ControllerModeEnum.SLIDER);
                Global.CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.SLIDER;
                AppUtils.putInt(PositionSettingsActivity.this, Global.CONTROLLER_MODE_POSITION_KEY, ControllerModeEnum.SLIDER.getIntValue());
                controllerModeChanged = true;
            }
        });

        binding.inputWaitingTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(binding.inputWaitingTime.getText().length() > 0){
                    position.setWaitingTime(Integer.parseInt(binding.inputWaitingTime.getText().toString()));
                }else{
                    position.setWaitingTime(100);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


    private void setControllerMode(ControllerModeEnum mode){
        if(mode == ControllerModeEnum.JOYSTICK){
            binding.lnJoystick.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgJoystick.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtJoystick.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnSlider.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgSlider.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtSlider.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.JOYSTICK;

        }else{
            binding.lnSlider.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgSlider.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtSlider.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnJoystick.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgJoystick.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtJoystick.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.SLIDER;
        }
    }

    private void setMotorSpeed(MotorSpeedEnum speed){
        if(speed == MotorSpeedEnum.SLOW){
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.SLOW;
            AppUtils.putInt(PositionSettingsActivity.this, Global.MOTOR_SPEED_POSITION_KEY, MotorSpeedEnum.SLOW.getIntValue());
            position.setMotorSpeed(MotorSpeedEnum.SLOW);
        }else if(speed == MotorSpeedEnum.NORMAL){
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.NORMAL;
            AppUtils.putInt(PositionSettingsActivity.this, Global.MOTOR_SPEED_POSITION_KEY, MotorSpeedEnum.NORMAL.getIntValue());
            position.setMotorSpeed(MotorSpeedEnum.NORMAL);
        }else{
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.white));

            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.FAST;
            AppUtils.putInt(PositionSettingsActivity.this, Global.MOTOR_SPEED_POSITION_KEY, MotorSpeedEnum.FAST.getIntValue());
            position.setMotorSpeed(MotorSpeedEnum.FAST);
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