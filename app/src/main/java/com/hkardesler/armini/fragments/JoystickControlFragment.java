/*
 * *
 *  * Created by Alper Kardesler on 5.06.2022 06:12
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.fragments;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hkardesler.armini.R;
import com.hkardesler.armini.activities.PositionSettingsActivity;
import com.hkardesler.armini.databinding.FragmentJoystickControlBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.helpers.JoyStickSurfaceView;
import com.hkardesler.armini.impls.ControllerModeChangeListener;
import com.hkardesler.armini.models.ArmInfo;
import com.hkardesler.armini.models.ArminiStatusEnum;
import com.hkardesler.armini.models.MotorSpeedEnum;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.User;

import java.lang.reflect.Type;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class JoystickControlFragment extends Fragment {

    FragmentJoystickControlBinding binding;
    Position mPosition;
    Position prevPosition = new Position();
    private static final String ARG_POSITION = "position";
    private static final String ARG_NEW_POSITION = "isNewPosition";
    private static final String ARG_SCENARIO_ID = "scenarioId";
    private ActivityResultLauncher<Intent> activityResultLauncher;
    DatabaseReference positionRef;
    String scenarioId;
    boolean isNewPosition = true;
    ControllerModeChangeListener mControllerModeChangeListener;
    int deletePositionId = -1;
    Mqtt5BlockingClient client;
    JoyStickSurfaceView.JoyStick joystick1LastState = JoyStickSurfaceView.JoyStick.NONE;
    JoyStickSurfaceView.JoyStick joystick2LastState = JoyStickSurfaceView.JoyStick.NONE;
    JoyStickSurfaceView.JoyStick joystick3LastState = JoyStickSurfaceView.JoyStick.NONE;
    String[] lastServoPositions;

    public JoystickControlFragment() {}

    public JoystickControlFragment(ControllerModeChangeListener controllerModeChangeListener) {
        mControllerModeChangeListener = controllerModeChangeListener;
    }

    public static JoystickControlFragment newInstance(String positionJson, boolean newPosition, String scenarioId, ControllerModeChangeListener controllerModeChangeListener) {
        JoystickControlFragment fragment = new JoystickControlFragment(controllerModeChangeListener);
        Bundle args = new Bundle();
        args.putString(ARG_POSITION, positionJson);
        args.putBoolean(ARG_NEW_POSITION, newPosition);
        args.putString(ARG_SCENARIO_ID, scenarioId);
        fragment.setArguments(args);

        return fragment;
    }

    public static JoystickControlFragment newInstance(ControllerModeChangeListener controllerModeChangeListener) {
        return new JoystickControlFragment(controllerModeChangeListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();

            String json = getArguments().getString(ARG_POSITION);
            Type type = new TypeToken<Position>() {}.getType();
            mPosition = gson.fromJson(json, type);

            prevPosition.setBase(mPosition.getBase());
            prevPosition.setShoulder(mPosition.getShoulder());
            prevPosition.setElbowVertical(mPosition.getElbowVertical());
            prevPosition.setElbowHorizontal(mPosition.getElbowHorizontal());
            prevPosition.setWristVertical(mPosition.getWristVertical());
            prevPosition.setWristHorizontal(mPosition.getWristHorizontal());
            prevPosition.setGripper(mPosition.getGripper());

            isNewPosition = getArguments().getBoolean(ARG_NEW_POSITION);
            scenarioId = getArguments().getString(ARG_SCENARIO_ID);
        }

        User user = AppUtils.getUser(getContext());
        positionRef = FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY)
                .child(user.getUserId()).child(Global.FIREBASE_SCENARIOS_KEY).child(scenarioId).child(Global.FIREBASE_POSITIONS_KEY)
                .child(String.valueOf(mPosition.getKey()));

        initActivityResultLauncher();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishActivity();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJoystickControlBinding.inflate(inflater, container, false);

        binding.txtTitle.setText(getString(R.string.position_name, (mPosition.getKey())+1));
        binding.sliderGripper.setValue(mPosition.getGripper());
        binding.txtGripperValue.setText(String.valueOf(mPosition.getGripper()));
        setListeners();
        backgroundTask();
        return binding.getRoot();
    }

    private void backgroundTask(){
        Observable.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        connectMQTTBroker();
                        client.subscribeWith()
                                .topicFilter(Global.MQTT_SERVO_POSITIONS_TOPIC)
                                .send();

                        client.toAsync().publishes(ALL, publish -> {
                            if(publish.getTopic().toString().equals(Global.MQTT_SERVO_POSITIONS_TOPIC)){
                                lastServoPositions = UTF_8.decode(publish.getPayload().get()).toString().split(";");
                                binding.sliderGripper.setValue(Integer.parseInt(lastServoPositions[lastServoPositions.length-1]));

                            }
                            System.out.println("Received message: " + publish.getTopic() + " -> " + UTF_8.decode(publish.getPayload().get()));
                        });

                        return true;
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                        if (aBoolean){
                            getMotorSpeedFromPrefs();
                            setJoystick();
                            if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE){
                                goToCurrentPosition();
                                AppUtils.updateInfoOnFirebase(ArminiStatusEnum.BUSY, AppUtils.getUser(requireContext()).getUserId());
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void getMotorSpeedFromPrefs() {
        int motorSpeedInt = AppUtils.getPrefs(requireContext()).getInt(Global.MOTOR_SPEED_POSITION_KEY, Global.MOTOR_SPEED_POSITION_VALUE.getIntValue());
        if(motorSpeedInt == MotorSpeedEnum.SLOW.getIntValue()){
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.SLOW;
        }else if(motorSpeedInt == MotorSpeedEnum.NORMAL.getIntValue()){
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.NORMAL;
        }else{
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.FAST;
        }
        sendMessageViaMQTT(Global.MQTT_MOTOR_SPEED_TOPIC, Global.MOTOR_SPEED_POSITION_VALUE.toString());

    }

    private void sendMessageViaMQTT(String topic, String msg){
        client.publishWith()
                .topic(topic)
                .payload(UTF_8.encode(msg))
                .send();
    }

    private void connectMQTTBroker(){
        client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(Global.MQTT_HOST)
                .serverPort(Global.MQTT_PORT)
                .sslWithDefaultConfig()
                .buildBlocking();

        client.connectWith()
                .simpleAuth()
                .username(Global.MQTT_USERNAME)
                .password(UTF_8.encode(Global.MQTT_PASSWORD))
                .applySimpleAuth()
                .send();
    }


    private void setListeners(){

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().finish();
            }
        });

        binding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), PositionSettingsActivity.class);
                Gson gson = new Gson();
                String positionJson = gson.toJson(mPosition);
                i.putExtra(Global.POSITION_KEY, positionJson);
                i.putExtra(Global.NEW_POSITION_KEY, isNewPosition);
                activityResultLauncher.launch(i);
            }
        });

        binding.btnHomePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToHomePosition();
            }
        });

        binding.sliderGripper.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setGripper(Math.round(value));
            binding.txtGripperValue.setText(String.valueOf(Math.round(value)));
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
                    binding.btnHelp.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gradientViolet), android.graphics.PorterDuff.Mode.SRC_IN);

                }else{
                    binding.imgHelpRobotBaseShoulder.setVisibility(View.GONE);
                    binding.imgHelpRobotElbow.setVisibility(View.GONE);
                    binding.imgHelpRobotWrist.setVisibility(View.GONE);

                    binding.joystickView1.setVisibility(View.VISIBLE);
                    binding.joystickView2.setVisibility(View.VISIBLE);
                    binding.joystickView3.setVisibility(View.VISIBLE);
                    binding.btnHelp.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_gray_2), android.graphics.PorterDuff.Mode.SRC_IN);

                }
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positionRef.child(Global.FIREBASE_MOTOR_SPEED_KEY).setValue(Global.MOTOR_SPEED_POSITION_VALUE.getIntValue());
                positionRef.child(Global.FIREBASE_BASE_KEY).setValue(mPosition.getBase());
                positionRef.child(Global.FIREBASE_WAITING_TIME_KEY).setValue(mPosition.getWaitingTime());
                positionRef.child(Global.FIREBASE_SHOULDER_KEY).setValue(mPosition.getShoulder());
                positionRef.child(Global.FIREBASE_ELBOW_VER_KEY).setValue(mPosition.getElbowVertical());
                positionRef.child(Global.FIREBASE_ELBOW_HOR_KEY).setValue(mPosition.getElbowHorizontal());
                positionRef.child(Global.FIREBASE_WRIST_VER_KEY).setValue(mPosition.getWristVertical());
                positionRef.child(Global.FIREBASE_WRIST_HOR_KEY).setValue(mPosition.getWristHorizontal());
                positionRef.child(Global.FIREBASE_GRIPPER_KEY).setValue(mPosition.getGripper()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        finishActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AppUtils.showToastMessage(getActivity(), getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                    }
                });
            }
        });

        binding.btnTryPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryPosition();
            }
        });
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

    private void goToHomePosition(){
        AppUtils.goToHomePosition(mPosition);
        binding.sliderGripper.setValue(mPosition.getGripper());
        sendMessageViaMQTT(Global.MQTT_FUNCTIONS_TOPIC, Global.MQTT_FUNCTIONS_GO_TO_HOME_KEY);
    }

    private void tryPosition(){
        String msg = prevPosition.getBase() + ";" + prevPosition.getShoulder() + ";" + prevPosition.getElbowVertical()
                + ";" + prevPosition.getElbowHorizontal() + ";" + prevPosition.getWristVertical() + ";" +
                prevPosition.getWristHorizontal() + ";" + prevPosition.getGripper() + ";" + prevPosition.getMotorSpeed().getIntValue() + ";" + prevPosition.getWaitingTime() + ";" +
                mPosition.getBase() + ";" + mPosition.getShoulder() + ";" + mPosition.getElbowVertical() +
                ";" + mPosition.getElbowHorizontal() + ";" + mPosition.getWristVertical() + ";" +
                mPosition.getWristHorizontal() + ";" + mPosition.getGripper() + ";" + mPosition.getMotorSpeed().getIntValue() + ";" + mPosition.getWaitingTime();

        sendMessageViaMQTT(Global.MQTT_TRY_POSITION_TOPIC, msg);
    }

    private void initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            deletePositionId = data.getIntExtra(Global.DELETE_POSITION_ID, -1);
                            if(deletePositionId != -1){
                                finishActivity();
                            }else{
                                getMotorSpeedFromPrefs();
                                Gson gson = new Gson();
                                String json = data.getStringExtra(Global.POSITION_KEY);
                                Type type = new TypeToken<Position>() {}.getType();
                                mPosition = gson.fromJson(json, type);
                                boolean controllerModeChanged = data.getBooleanExtra(Global.CONTROLLER_MODE_CHANGED, false);
                                if(controllerModeChanged)
                                    mControllerModeChangeListener.onControllerModeChanged();
                            }
                        }
                    }
                });
    }

    private void finishActivity(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Global.DELETE_POSITION_ID, deletePositionId);
        requireActivity().setResult(Activity.RESULT_OK, resultIntent);
        requireActivity().finish();
    }

    private void goToCurrentPosition(){
        String msg = mPosition.getBase() + ";" + mPosition.getShoulder() + ";" + mPosition.getElbowVertical() +
                ";" + mPosition.getElbowHorizontal() + ";" + mPosition.getWristVertical() + ";" +
                mPosition.getWristHorizontal() + ";" + mPosition.getGripper() + ";" + mPosition.getMotorSpeed();

        sendMessageViaMQTT(Global.MQTT_GO_TO_POSITION_TOPIC, msg);
    }
}