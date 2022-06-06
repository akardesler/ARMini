/*
 * *
 *  * Created by Haydar Kardesler on 3.06.2022 01:51
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.adapters.PositionAdapter;
import com.hkardesler.armini.databinding.ActivityScenarioBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.impls.PositionItemClickListener;
import com.hkardesler.armini.models.MotorSpeed;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.Scenario;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ScenarioActivity extends BaseActivity implements PositionItemClickListener {
    ActivityScenarioBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    Scenario scenario;
    PositionAdapter positionAdapter;
    DatabaseReference positionsRef;
    ArrayList<Position> positions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScenarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String json = getIntent().getExtras().getString(Global.SCENARIO_KEY);
        Type type = new TypeToken<Scenario>() {}.getType();
        scenario = gson.fromJson(json, type);

        positionsRef = FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY)
                .child(user.getUserId()).child(Global.FIREBASE_SCENARIOS_KEY).child(scenario.getId()).child(Global.FIREBASE_POSITIONS_KEY);
        binding.txtTitle.setText(scenario.getName());
        binding.rvPositions.setAdapter(null);

        initActivityResultLauncher();
        setListeners();
        getPositionsFromFirebase();
        getMotorSpeedFromPrefs();
    }

    @Override
    protected void setListeners() {
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
                activityResultLauncher.launch(i);
            }
        });

        binding.btnAddPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ScenarioActivity.this, PositionActivity.class);
                Position position = new Position();
                position.setKey(positions.size());
                position.setMotorSpeed(Global.MOTOR_SPEED_POSITION_VALUE);

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

                activityResultLauncher.launch(i);
            }
        });
    }


    private void initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
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
                        }
                    }
                });
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
        Intent i = new Intent(ScenarioActivity.this, PositionActivity.class);
        Position positionObj = positions.get(position);
        String jsonPosition = AppUtils.convertJson(positionObj);
        i.putExtra(Global.POSITION_KEY, jsonPosition);
        i.putExtra(Global.NEW_POSITION_KEY, false);
        i.putExtra(Global.SCENARIO_ID_KEY, scenario.getId());

        activityResultLauncher.launch(i);
    }

    private void getPositionsFromFirebase(){
        positionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                positions = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Position position = postSnapshot.getValue(Position.class);
                    String key = postSnapshot.getKey();
                    assert position != null;
                    assert key != null;
                    position.setKey(Long.parseLong(key));
                    positions.add(position);
                }
                setRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                binding.txtListEmpty.setVisibility(View.VISIBLE);

            }
        });
    }

    private void setRecyclerView() {
        binding.cardviewLoading.setVisibility(View.GONE);
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

    }

    private void getMotorSpeedFromPrefs() {
        int motorSpeedInt = prefs.getInt(Global.MOTOR_SPEED_POSITION_KEY, Global.MOTOR_SPEED_POSITION_VALUE.getIntValue());
        if(motorSpeedInt == MotorSpeed.SLOW.getIntValue()){
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeed.SLOW;
        }else if(motorSpeedInt == MotorSpeed.NORMAL.getIntValue()){
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeed.NORMAL;
        }else{
            Global.MOTOR_SPEED_POSITION_VALUE = MotorSpeed.FAST;
        }
    }
}