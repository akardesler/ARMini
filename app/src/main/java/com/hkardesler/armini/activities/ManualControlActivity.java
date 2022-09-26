/*
 * *
 *  * Created by Alper Kardesler on 8.06.2022 04:48
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityManualControlBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.helpers.JoyStickSurfaceView;
import com.hkardesler.armini.helpers.MQTTBroker;
import com.hkardesler.armini.impls.ArminiStatusChangeListener;
import com.hkardesler.armini.impls.MqttBrokerCallback;
import com.hkardesler.armini.models.ArmInfo;
import com.hkardesler.armini.models.ArminiStatusEnum;
import com.hkardesler.armini.models.MotorSpeedEnum;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ManualControlActivity extends BaseActivity implements MqttBrokerCallback {

    ActivityManualControlBinding binding;
    JoyStickSurfaceView.JoyStick joystick1LastState = JoyStickSurfaceView.JoyStick.NONE;
    JoyStickSurfaceView.JoyStick joystick2LastState = JoyStickSurfaceView.JoyStick.NONE;
    JoyStickSurfaceView.JoyStick joystick3LastState = JoyStickSurfaceView.JoyStick.NONE;
    boolean isLightOn = false;
    MQTTBroker mqttBroker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManualControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mqttBroker = new MQTTBroker();
        mqttBroker.setCallback(this);

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
                if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE){
                    goToHomePosition();
                }
                finish();
            }
        });
        binding.btnHomePosition.setOnClickListener(view -> goToHomePosition());
        binding.btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLightOn){
                    binding.btnLight.setImageDrawable(ContextCompat.getDrawable(ManualControlActivity.this, R.drawable.ic_light_on_gradient));
                    isLightOn = false;
                    sendMessageViaMQTT(Global.MQTT_FUNCTIONS_TOPIC, Global.MQTT_FUNCTIONS_OFF_LIGHT_KEY);
                }else{
                    binding.btnLight.setImageDrawable(ContextCompat.getDrawable(ManualControlActivity.this, R.drawable.ic_light_off_gradient));
                    isLightOn = true;
                    sendMessageViaMQTT(Global.MQTT_FUNCTIONS_TOPIC, Global.MQTT_FUNCTIONS_ON_LIGHT_KEY);
                }

            }
        });

        binding.sliderGripper.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                binding.txtGripperValue.setText(String.valueOf(Math.round(value)));
                sendMessageViaMQTT(Global.MQTT_GRIPPER_TOPIC, String.valueOf(Math.round(value)));
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

        binding.btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.imgHelpRobotBaseShoulder.getVisibility() == View.GONE){
                    binding.imgHelpRobotBaseShoulder.setVisibility(View.VISIBLE);
                    binding.imgHelpRobotElbow.setVisibility(View.VISIBLE);
                    binding.imgHelpRobotWrist.setVisibility(View.VISIBLE);

                    binding.joystickView1.setVisibility(View.GONE);
                    binding.joystickView2.setVisibility(View.GONE);
                    binding.joystickView3.setVisibility(View.GONE);
                    binding.btnHelp.setColorFilter(ContextCompat.getColor(ManualControlActivity.this, R.color.gradientViolet), android.graphics.PorterDuff.Mode.SRC_IN);

                }else{
                    binding.imgHelpRobotBaseShoulder.setVisibility(View.GONE);
                    binding.imgHelpRobotElbow.setVisibility(View.GONE);
                    binding.imgHelpRobotWrist.setVisibility(View.GONE);

                    binding.joystickView1.setVisibility(View.VISIBLE);
                    binding.joystickView2.setVisibility(View.VISIBLE);
                    binding.joystickView3.setVisibility(View.VISIBLE);
                    binding.btnHelp.setColorFilter(ContextCompat.getColor(ManualControlActivity.this, R.color.dark_gray_2), android.graphics.PorterDuff.Mode.SRC_IN);

                }
            }
        });


    }

    private void goToHomePosition(){
        sendMessageViaMQTT(Global.MQTT_FUNCTIONS_TOPIC, Global.MQTT_FUNCTIONS_GO_TO_HOME_KEY);
    }


    private void setJoysticks(){
        binding.joystickView1.setOnJoyStickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStick state) {
                if(joystick1LastState != state){
                    joystick1LastState = state;
                    sendMessageViaMQTT(Global.MQTT_JOYSTICK_1_TOPIC, joystick1LastState.toString());
                }
            }
        }, JoyStickSurfaceView.LOOP_INTERVAL_SLOW, JoyStickSurfaceView.LOOP_INTERVAL_FAST);

        binding.joystickView1.setOnChangeStateListener(new JoyStickSurfaceView.OnChangeStateListener() {
            @Override
            public void onChangeState(JoyStickSurfaceView.JoyStick next,
                                      JoyStickSurfaceView.JoyStick previous) {

            }
        });

        binding.joystickView2.setOnJoyStickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStick state) {
                if(joystick2LastState != state){
                    joystick2LastState = state;
                    sendMessageViaMQTT(Global.MQTT_JOYSTICK_2_TOPIC, joystick2LastState.toString());
                }
            }
        }, JoyStickSurfaceView.LOOP_INTERVAL_SLOW, JoyStickSurfaceView.LOOP_INTERVAL_FAST);

        binding.joystickView2.setOnChangeStateListener(new JoyStickSurfaceView.OnChangeStateListener() {
            @Override
            public void onChangeState(JoyStickSurfaceView.JoyStick next,
                                      JoyStickSurfaceView.JoyStick previous) {

            }
        });

        binding.joystickView3.setOnJoyStickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStick state) {
                if(joystick3LastState != state){
                    joystick3LastState = state;
                    sendMessageViaMQTT(Global.MQTT_JOYSTICK_3_TOPIC, joystick3LastState.toString());
                }
            }
        }, JoyStickSurfaceView.LOOP_INTERVAL_SLOW, JoyStickSurfaceView.LOOP_INTERVAL_FAST);

        binding.joystickView3.setOnChangeStateListener(new JoyStickSurfaceView.OnChangeStateListener() {
            @Override
            public void onChangeState(JoyStickSurfaceView.JoyStick next,
                                      JoyStickSurfaceView.JoyStick previous) {

            }
        });
    }

    private void sendMessageViaMQTT(String topic, String msg){
        mqttBroker.getClient().publishWith()
                .topic(topic)
                .payload(UTF_8.encode(msg))
                .send();
    }

    private void getMotorSpeedFromPrefs() {
        int motorSpeedInt = prefs.getInt(Global.MOTOR_SPEED_MANUAL_KEY, Global.MOTOR_SPEED_MANUAL_VALUE.getIntValue());
        if(motorSpeedInt == MotorSpeedEnum.SLOW.getIntValue()){
            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.SLOW;
        }else if(motorSpeedInt == MotorSpeedEnum.NORMAL.getIntValue()){
            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.NORMAL;
        }else{
            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.FAST;
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

            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.SLOW;
        }else if(speed == MotorSpeedEnum.NORMAL){
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.NORMAL;
        }else{
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.white));

            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.FAST;
        }

        AppUtils.putInt(this, Global.MOTOR_SPEED_MANUAL_KEY, Global.MOTOR_SPEED_MANUAL_VALUE.getIntValue());
        sendMessageViaMQTT(Global.MQTT_MOTOR_SPEED_TOPIC, Global.MOTOR_SPEED_MANUAL_VALUE.toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE){
            goToHomePosition();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE) {
            AppUtils.updateInfoOnFirebase(ArminiStatusEnum.AVAILABLE);
        }
    }

    //MQTT BROKER CALLBACK
    @Override
    public void onComplete() {
        mqttBroker.getClient().subscribeWith()
                .topicFilter(Global.MQTT_SERVO_POSITIONS_TOPIC)
                .send();

        mqttBroker.getClient().toAsync().publishes(ALL, publish -> {
            // This method gets callback from MQTT topics
            if(publish.getTopic().toString().equals(Global.MQTT_SERVO_POSITIONS_TOPIC)){
                String[] positions = UTF_8.decode(publish.getPayload().get()).toString().split(";");
                binding.sliderGripper.setValue(Integer.parseInt(positions[positions.length-1]));
            }
            System.out.println("Received message: " + publish.getTopic() + " -> " + UTF_8.decode(publish.getPayload().get()));
        });

        getMotorSpeedFromPrefs();
        setMotorSpeed(Global.MOTOR_SPEED_MANUAL_VALUE);
        setJoysticks();
        if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE){
            goToHomePosition();
        }
        AppUtils.updateInfoOnFirebase(ArminiStatusEnum.BUSY, user.getUserId());
    }

    @Override
    public void onError() {

    }
}