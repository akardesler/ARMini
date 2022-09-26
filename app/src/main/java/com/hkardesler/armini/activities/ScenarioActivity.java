/*
 * *
 *  * Created by Alper Kardesler on 3.06.2022 01:51
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hkardesler.armini.R;
import com.hkardesler.armini.adapters.PositionAdapter;
import com.hkardesler.armini.databinding.ActivityScenarioBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.helpers.MQTTBroker;
import com.hkardesler.armini.impls.ArminiStatusChangeListener;
import com.hkardesler.armini.impls.MqttBrokerCallback;
import com.hkardesler.armini.impls.PositionItemClickListener;
import com.hkardesler.armini.models.ArmInfo;
import com.hkardesler.armini.models.ArminiStatusEnum;
import com.hkardesler.armini.models.MotorSpeedEnum;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.Scenario;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ScenarioActivity extends BaseActivity implements PositionItemClickListener, MqttBrokerCallback {
    ActivityScenarioBinding binding;
    private ActivityResultLauncher<Intent> settingsActivityResultLauncher;
    private ActivityResultLauncher<Intent> positionActivityResultLauncher;
    Scenario scenario;
    PositionAdapter positionAdapter;
    DatabaseReference positionsRef;
    ArrayList<Position> positions;
    boolean isScenarioRunning = false;
    MQTTBroker mqttBroker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScenarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppUtils.showLoading(this, binding.coordinatorLayout);
        String json = getIntent().getExtras().getString(Global.SCENARIO_KEY);
        Type type = new TypeToken<Scenario>() {}.getType();
        scenario = gson.fromJson(json, type);

        positionsRef = FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY)
                .child(user.getUserId()).child(Global.FIREBASE_SCENARIOS_KEY).child(scenario.getId()).child(Global.FIREBASE_POSITIONS_KEY);
        binding.txtTitle.setText(scenario.getName());
        mqttBroker = new MQTTBroker();
        mqttBroker.setCallback(this);
        initActivityResultLauncher();
        setListeners();
        getPositionsFromFirebase();
        getMotorSpeedFromPrefs();

    }

    @Override
    protected void setListeners() {
        ArmInfo.addArminiStatusChangedListener(new ArminiStatusChangeListener() {
            @Override
            public void OnArminiStatusChanged(ArminiStatusEnum status) {
                if(status == ArminiStatusEnum.OFFLINE){
                    binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.color.gray1));
                    binding.txtRunStop.setText(getString(R.string.run));
                    isScenarioRunning = false;
                }else if(status == ArminiStatusEnum.BUSY){
                    if(isScenarioRunning){
                        binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.gradient_bg_button));
                    }else{
                        binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.color.gray1));
                    }
                }else{
                    binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.gradient_bg_button));
                    binding.imgRunStop.setImageDrawable(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.ic_play));
                    binding.txtRunStop.setText(getString(R.string.run));
                }

            }
        });

        binding.cardRunStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((isScenarioRunning && !ArmInfo.getScenarioId().equals(scenario.getId())) || positions.size() == 0 || ArmInfo.getStatus() == ArminiStatusEnum.OFFLINE){
                    return;
                }

                if(isScenarioRunning){
                    //stop code
                    AppUtils.sendMessageViaMQTT(mqttBroker.getClient(), Global.MQTT_STOP_SCENARIO_TOPIC, scenario.getId());
                    AppUtils.updateInfoOnFirebase(ArminiStatusEnum.AVAILABLE, "", "");
                    binding.imgRunStop.setImageDrawable(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.ic_play));
                    binding.txtRunStop.setText(getString(R.string.run));
                    isScenarioRunning = false;
                }else{
                   // AppUtils.showLoading(ScenarioActivity.this, binding.coordinatorLayout);
                    //run code
                    String msg = user.getFullName()+";"+user.getUserId()+";"+scenario.getId();
                    AppUtils.sendMessageViaMQTT(mqttBroker.getClient(), Global.MQTT_RUN_SCENARIO_TOPIC, msg);

                    AppUtils.updateInfoOnFirebase(ArminiStatusEnum.BUSY, user.getUserId(), scenario.getId());
                    binding.imgRunStop.setImageDrawable(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.ic_stop));
                    binding.txtRunStop.setText(getString(R.string.stop));

                    isScenarioRunning = true;
                }
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
            }
        });

        binding.cardviewScenarioSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String json = AppUtils.convertJson(scenario);
                Intent i = new Intent(ScenarioActivity.this, ScenarioSettingsActivity.class);
                i.putExtra(Global.SCENARIO_KEY, json);
                settingsActivityResultLauncher.launch(i);
            }
        });

        binding.btnAddPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY){
                    AppUtils.showToastMessage(ScenarioActivity.this, getString(R.string.armini_busy), R.drawable.ic_error, R.color.blue_500);
                    return;
                }else if(ArmInfo.getStatus() == ArminiStatusEnum.OFFLINE){
                    AppUtils.showToastMessage(ScenarioActivity.this, getString(R.string.armini_offline), R.drawable.ic_error, R.color.blue_500);
                    return;
                }

                Intent i = new Intent(ScenarioActivity.this, PositionActivity.class);
                Position position = new Position();
                position.setKey(positions.size());
                position.setMotorSpeed(Global.MOTOR_SPEED_POSITION_VALUE);
                position.setWaitingTime(Global.POSITION_WAITING_TIME_VALUE);

                Position prevPosition;
                if(positions.size() > 0){
                    prevPosition = positions.get(positions.size()-1);
                }else{
                    prevPosition = AppUtils.getHomePosition();
                }

                position.setBase(prevPosition.getBase());
                position.setShoulder(prevPosition.getShoulder());
                position.setElbowVertical(prevPosition.getElbowVertical());
                position.setElbowHorizontal(prevPosition.getElbowHorizontal());
                position.setWristVertical(prevPosition.getWristVertical());
                position.setWristHorizontal(prevPosition.getWristHorizontal());
                position.setGripper(prevPosition.getGripper());

                String jsonPosition = AppUtils.convertJson(position);
                i.putExtra(Global.POSITION_KEY, jsonPosition);
                i.putExtra(Global.NEW_POSITION_KEY, true);
                i.putExtra(Global.SCENARIO_ID_KEY, scenario.getId());

                positionActivityResultLauncher.launch(i);
            }
        });
    }


    private void initActivityResultLauncher() {
        settingsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                           String scenarioJson = data.getStringExtra(Global.SCENARIO_KEY);
                           if(scenarioJson != null) {
                               Type type = new TypeToken<Scenario>() {
                               }.getType();
                               scenario = gson.fromJson(scenarioJson, type);
                               binding.txtTitle.setText(scenario.getName());
                           }

                            boolean scenarioDeleted = data.getBooleanExtra(Global.SCENARIO_DELETED_KEY, false);
                            if(scenarioDeleted)
                                finishActivity();

                            getPositionsFromFirebase();

                        }
                    }
                });

        positionActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            int deletePositionId = data.getIntExtra(Global.DELETE_POSITION_ID, -1);
                            if(deletePositionId != -1){
                                deletePosition(deletePositionId);
                            }
                            getPositionsFromFirebase();
                        }
                    }
                });
    }

    private void deletePosition(int positionId){
        for (int i = positionId; i < positions.size(); i++){
            int finalI = i;
            positionsRef.child(String.valueOf(positions.get(i).getKey())).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    if(finalI == positions.size()-1){
                        AppUtils.showToastMessage(ScenarioActivity.this,getString(R.string.position_deleted), R.drawable.ic_done, R.color.green_500);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    if(finalI == positions.size()-1){
                        AppUtils.showToastMessage(ScenarioActivity.this, getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void finishActivity(){
        //Intent resultIntent = new Intent();
       // resultIntent.putExtra(Global.REFRESH_MAIN_ACTIVITY_KEY, refreshMainActivity);
        //setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onItemClicked(int position) {

        if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY){
            AppUtils.showToastMessage(ScenarioActivity.this, getString(R.string.armini_busy), R.drawable.ic_error, R.color.blue_500);
            return;
        }else if(ArmInfo.getStatus() == ArminiStatusEnum.OFFLINE){
            AppUtils.showToastMessage(ScenarioActivity.this, getString(R.string.armini_offline), R.drawable.ic_error, R.color.blue_500);
            return;
        }

        Intent i = new Intent(ScenarioActivity.this, PositionActivity.class);
        Position positionObj = positions.get(position);
        String jsonPosition = AppUtils.convertJson(positionObj);
        i.putExtra(Global.POSITION_KEY, jsonPosition);
        i.putExtra(Global.NEW_POSITION_KEY, false);
        i.putExtra(Global.SCENARIO_ID_KEY, scenario.getId());

        positionActivityResultLauncher.launch(i);
    }

    private void getPositionsFromFirebase(){
        positionsRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                positions = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                    Position position = new Position();

                    position.setKey(Integer.parseInt(Objects.requireNonNull(postSnapshot.getKey())));

                    int motorSpeed = Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_MOTOR_SPEED_KEY).getValue(Long.class)).intValue();
                    if(motorSpeed == MotorSpeedEnum.SLOW.getIntValue()){
                        position.setMotorSpeed(MotorSpeedEnum.SLOW);
                    }else if(motorSpeed == MotorSpeedEnum.NORMAL.getIntValue()){
                        position.setMotorSpeed(MotorSpeedEnum.NORMAL);
                    }else {
                        position.setMotorSpeed(MotorSpeedEnum.FAST);
                    }
                    position.setWaitingTime(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_WAITING_TIME_KEY).getValue(Long.class)).intValue());
                    position.setBase(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_BASE_KEY).getValue(Long.class)).intValue());
                    position.setShoulder(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_SHOULDER_KEY).getValue(Long.class)).intValue());
                    position.setElbowVertical(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_ELBOW_VER_KEY).getValue(Long.class)).intValue());
                    position.setElbowHorizontal(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_ELBOW_HOR_KEY).getValue(Long.class)).intValue());
                    position.setWristVertical(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_WRIST_VER_KEY).getValue(Long.class)).intValue());
                    position.setWristHorizontal(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_WRIST_HOR_KEY).getValue(Long.class)).intValue());
                    position.setGripper(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_GRIPPER_KEY).getValue(Long.class)).intValue());

                    positions.add(position);
                }

                setRecyclerView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtils.showToastMessage(ScenarioActivity.this,getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);

            }
        });

    }

    private void setRecyclerView() {
        AppUtils.hideLoading(this, binding.coordinatorLayout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.rvPositions.setLayoutManager(gridLayoutManager);
        positionAdapter = new PositionAdapter(this, positions,this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvPositions.setLayoutManager(layoutManager);
        positionAdapter = new PositionAdapter(this, positions, this);
        binding.rvPositions.setAdapter(positionAdapter);

        if(positions.size() > 0){
            binding.txtListEmpty.setVisibility(View.GONE);
        }else{
            binding.txtListEmpty.setVisibility(View.VISIBLE);
        }
        setRunStopButton();
    }

    private void setRunStopButton(){
        if(ArmInfo.getStatus() == ArminiStatusEnum.OFFLINE){
            binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.color.gray1));
            binding.txtRunStop.setText(getString(R.string.run));
            isScenarioRunning = false;
        }else if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY){
            if(isScenarioRunning){
                binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.gradient_bg_button));
            }else{
                binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.color.gray1));
            }
        }else{
            binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.gradient_bg_button));
            binding.imgRunStop.setImageDrawable(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.ic_play));
            binding.txtRunStop.setText(getString(R.string.run));
        }
    }

    private void getMotorSpeedFromPrefs() {
        int motorSpeedInt = prefs.getInt(Global.MOTOR_SPEED_POSITION_KEY, Global.MOTOR_SPEED_POSITION_VALUE.getIntValue());
        if(motorSpeedInt == MotorSpeedEnum.SLOW.getIntValue()){
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.SLOW;
        }else if(motorSpeedInt == MotorSpeedEnum.NORMAL.getIntValue()){
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.NORMAL;
        }else{
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.FAST;
        }
    }

    @Override
    public void onComplete() {
        if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY && ArmInfo.getOperatorId().equals(user.getUserId()) && ArmInfo.getScenarioId().equals(scenario.getId())){
            binding.imgRunStop.setImageDrawable(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.ic_stop));
            binding.txtRunStop.setText(getString(R.string.stop));
            isScenarioRunning = true;
            binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.gradient_bg_button));

        }else if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY && !ArmInfo.getScenarioId().equals("") && !ArmInfo.getScenarioId().equals(scenario.getId())){
            binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.color.gray1));
            binding.txtRunStop.setText(getString(R.string.run));
            isScenarioRunning = false;
        }else if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE){
            binding.lnRunStop.setBackground(ContextCompat.getDrawable(ScenarioActivity.this, R.drawable.gradient_bg_button));
            binding.txtRunStop.setText(getString(R.string.run));
            isScenarioRunning = false;
        }
        /*
        mqttBroker.getClient().subscribeWith()
                .topicFilter(Global.MQTT_SCENARIO_STARTED_CALLBACK_TOPIC)
                .send();
        mqttBroker.getClient().toAsync().publishes(ALL, publish -> {
            // This method gets callback from MQTT topics
            if(publish.getTopic().toString().equals(Global.MQTT_SCENARIO_STARTED_CALLBACK_TOPIC)){
              //  AppUtils.hideLoading(ScenarioActivity.this, binding.coordinatorLayout);
            }
        });
        */
    }

    @Override
    public void onError() {

    }
}