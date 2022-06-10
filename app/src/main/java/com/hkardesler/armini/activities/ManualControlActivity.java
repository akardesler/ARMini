/*
 * *
 *  * Created by Haydar Kardesler on 8.06.2022 04:48
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Bundle;
import android.util.Log;
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
import com.hkardesler.armini.models.ArmStatus;
import com.hkardesler.armini.models.MotorSpeed;

public class ManualControlActivity extends BaseActivity {

    ActivityManualControlBinding binding;
    Mqtt5BlockingClient client;
    JoyStickSurfaceView.JoyStick joystick1LastState = JoyStickSurfaceView.JoyStick.NONE;
    JoyStickSurfaceView.JoyStick joystick2LastState = JoyStickSurfaceView.JoyStick.NONE;
    JoyStickSurfaceView.JoyStick joystick3LastState = JoyStickSurfaceView.JoyStick.NONE;
    boolean isLightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        connectMQTTBroker();
        client.subscribeWith()
                .topicFilter(Global.MQTT_SERVO_POSITIONS_TOPIC)
                .send();
        client.toAsync().publishes(ALL, publish -> {
            String[] positions = UTF_8.decode(publish.getPayload().get()).toString().split(";");
            binding.sliderGripper.setValue(Float.parseFloat(positions[positions.length-1]));
            System.out.println("Received message: " + publish.getTopic() + " -> " + UTF_8.decode(publish.getPayload().get()));
        });

        getMotorSpeedFromPrefs();
        setMotorSpeed(Global.MOTOR_SPEED_MANUAL_VALUE);
        setJoystick();

        if(Global.ARMINI_STATUS == ArmStatus.AVAILABLE){
            goToHomePosition();
        }
    }

    @Override
    protected void setListeners() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Global.ARMINI_STATUS == ArmStatus.AVAILABLE){
                    goToHomePosition();
                }
                finish();
            }
        });
        binding.btnHomePosition.setOnClickListener(view -> goToHomePosition());
        binding.btnLigt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLightOn){
                    binding.btnLigt.setImageDrawable(ContextCompat.getDrawable(ManualControlActivity.this, R.drawable.ic_light_on_gradient));
                    isLightOn = false;
                    sendMessageViaMQTT(Global.MQTT_FUNCTIONS_TOPIC, Global.MQTT_FUNCTIONS_OFF_LIGHT_KEY);
                }else{
                    binding.btnLigt.setImageDrawable(ContextCompat.getDrawable(ManualControlActivity.this, R.drawable.ic_light_off_gradient));
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

    private void connectMQTTBroker(){
        client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(Global.MQTT_HOST)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();

        //connect to HiveMQ Cloud with TLS and username/pw
        client.connectWith()
                .simpleAuth()
                .username(Global.MQTT_USERNAME)
                .password(UTF_8.encode(Global.MQTT_PASSWORD))
                .applySimpleAuth()
                .send();
    }

    private void setJoystick(){
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
        client.publishWith()
                .topic(topic)
                .payload(UTF_8.encode(msg))
                .send();
    }

    private void getMotorSpeedFromPrefs() {
        int motorSpeedInt = prefs.getInt(Global.MOTOR_SPEED_MANUAL_KEY, Global.MOTOR_SPEED_MANUAL_VALUE.getIntValue());
        if(motorSpeedInt == MotorSpeed.SLOW.getIntValue()){
            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.SLOW;
        }else if(motorSpeedInt == MotorSpeed.NORMAL.getIntValue()){
            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.NORMAL;
        }else{
            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.FAST;
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

            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.SLOW;
            AppUtils.putInt(this, Global.MOTOR_SPEED_MANUAL_KEY, MotorSpeed.SLOW.getIntValue());
            sendMessageViaMQTT(Global.MQTT_MOTOR_SPEED_TOPIC, MotorSpeed.SLOW.toString());
        }else if(speed == MotorSpeed.NORMAL){
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.NORMAL;
            AppUtils.putInt(this, Global.MOTOR_SPEED_MANUAL_KEY, MotorSpeed.NORMAL.getIntValue());
            sendMessageViaMQTT(Global.MQTT_MOTOR_SPEED_TOPIC, MotorSpeed.NORMAL.toString());

        }else{
            binding.lnSlow.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtSlow.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnNormal.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.txtNormal.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            binding.lnFast.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.txtFast.setTextColor(ContextCompat.getColor(this, R.color.white));

            Global.MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.FAST;
            AppUtils.putInt(this, Global.MOTOR_SPEED_MANUAL_KEY, MotorSpeed.FAST.getIntValue());
            sendMessageViaMQTT(Global.MQTT_MOTOR_SPEED_TOPIC, MotorSpeed.FAST.toString());

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(Global.ARMINI_STATUS == ArmStatus.AVAILABLE){
            goToHomePosition();
        }
        finish();
    }
}