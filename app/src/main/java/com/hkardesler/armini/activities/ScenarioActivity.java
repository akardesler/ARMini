/*
 * *
 *  * Created by Haydar Kardesler on 3.06.2022 01:51
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
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
import com.hkardesler.armini.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ScenarioActivity extends AppCompatActivity implements PositionItemClickListener {
    ActivityScenarioBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    Scenario scenario;
    RecyclerView rvPositions;
    PositionAdapter positionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScenarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayList<Position> positions = new ArrayList<>();
        Position p = new Position();
        p.setKey("1");
        p.setMotorSpeed(MotorSpeed.FAST);
        positions.add(p);
        positions.add(p);
        positions.add(p);

        rvPositions = binding.rvPositions;
        positionAdapter = new PositionAdapter(this, positions,this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvPositions.setLayoutManager(layoutManager);
        rvPositions.setAdapter(positionAdapter);

        Log.e("--","adapter "+ positionAdapter.getItemCount());
        Gson gson = new Gson();
        String json = getIntent().getExtras().getString(Global.SCENARIO_KEY);
        Type type = new TypeToken<Scenario>() {}.getType();
        scenario = gson.fromJson(json, type);
        binding.txtTitle.setText(scenario.getName());
        initActivityResultLauncher();
        setListeners();
    }

    private void setListeners(){
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
    }

    private void initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                           String scenarioJson = data.getStringExtra(Global.SCENARIO_KEY);
                           Gson gson = new Gson();
                           Type type = new TypeToken<Scenario>() {}.getType();
                           scenario = gson.fromJson(scenarioJson, type);
                           binding.txtTitle.setText(scenario.getName());

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

    }
}