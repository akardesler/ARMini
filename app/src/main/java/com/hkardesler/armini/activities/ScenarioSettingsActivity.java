/*
 * *
 *  * Created by Haydar Kardesler on 3.06.2022 05:10
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityScenarioSettingsBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.models.Scenario;
import com.hkardesler.armini.models.User;
import com.hkardesler.armini.models.WorkingMode;

import java.lang.reflect.Type;

public class ScenarioSettingsActivity extends AppCompatActivity {
    ActivityScenarioSettingsBinding binding;
    WorkingMode workingMode = WorkingMode.INFINITE;
    boolean isLightOn = false;
    Scenario scenario;
    DatabaseReference scenarioRef;
    boolean scenarioDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScenarioSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Gson gson = new Gson();
        String json = getIntent().getExtras().getString(Global.SCENARIO_KEY);
        Type type = new TypeToken<Scenario>() {}.getType();
        scenario = gson.fromJson(json, type);

        User user = AppUtils.getUser(this);

        scenarioRef = FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY).child(user.getUserId()).child(Global.FIREBASE_SCENARIOS_KEY).child(scenario.getId());
        if(scenario.getWorkingMode() == WorkingMode.INFINITE){
            workingMode = WorkingMode.INFINITE;
            setWorkingMode(WorkingMode.INFINITE);

        }else{
            workingMode = WorkingMode.LOOP;
            setWorkingMode(WorkingMode.LOOP);
        }

        isLightOn = scenario.isLightOpen();
        setLight(isLightOn);

        binding.inputLoopCount.setText(String.valueOf(scenario.getLoopCount()));

        binding.inputScenarioName.setText(scenario.getName());
        setListeners();
    }

    private void setListeners() {
        binding.cardInfinite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWorkingMode(WorkingMode.INFINITE);
            }
        });

        binding.cardLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWorkingMode(WorkingMode.LOOP);
            }
        });

        binding.cardOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLight(true);
            }
        });

        binding.cardOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLight(false);
            }
        });

        binding.btnDeleteScenario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scenarioRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        scenarioDeleted = true;
                        AppUtils.showToastMessage(ScenarioSettingsActivity.this,getString(R.string.scenario_deleted), R.drawable.ic_done, R.color.green_500);
                        finishActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AppUtils.showToastMessage(ScenarioSettingsActivity.this, getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);                    }
                });

            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDataOnFirebase();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
            }
        });
    }

    private void setWorkingMode(WorkingMode mode){
        if(mode == WorkingMode.INFINITE){
            binding.lnInfinite.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgInfinite.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtInfinite.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnLoop.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgLoop.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtLoop.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));

            workingMode = WorkingMode.INFINITE;

            binding.rltLoopCount.setVisibility(View.GONE);
        }else{
            binding.lnLoop.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgLoop.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtLoop.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnInfinite.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgInfinite.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtInfinite.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));
            workingMode = WorkingMode.LOOP;

            binding.rltLoopCount.setVisibility(View.VISIBLE);

        }
    }

    private void setLight(boolean on){
        if(on){
            binding.lnLightOn.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgOn.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtOn.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnLightOff.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgOff.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtOff.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));
            isLightOn = true;
        }else{
            binding.lnLightOff.setBackground(ContextCompat.getDrawable(this, R.color.dark_gray_2));
            binding.imgOff.setColorFilter(ContextCompat.getColor(this, R.color.white));
            binding.txtOff.setTextColor(ContextCompat.getColor(this, R.color.white));

            binding.lnLightOn.setBackground(ContextCompat.getDrawable(this, R.color.white));
            binding.imgOn.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray_2));
            binding.txtOn.setTextColor(ContextCompat.getColor(this, R.color.dark_gray_2));
            isLightOn = false;
        }
    }

    private void setDataOnFirebase(){
        String name = binding.inputScenarioName.getText().toString();
        String loopCount = binding.inputLoopCount.getText().toString();
        if(name.trim().isEmpty()){
            AppUtils.showToastMessage(this, getString(R.string.scenario_name_warning), R.drawable.ic_close, R.color.red);
            return;
        }
        if(loopCount.isEmpty()){
            AppUtils.showToastMessage(this, getString(R.string.loop_count_warning), R.drawable.ic_close, R.color.red);
            return;
        }

        scenario.setName(name);
        scenario.setWorkingMode(workingMode);
        scenario.setLoopCount(Integer.parseInt(loopCount));
        scenario.setLightOpen(isLightOn);
        scenarioRef.child(Global.FIREBASE_SCENARIO_NAME_KEY).setValue(scenario.getName());
        scenarioRef.child(Global.FIREBASE_WORKING_MODE_KEY).setValue(scenario.getWorkingMode());
        scenarioRef.child(Global.FIREBASE_LOOP_COUNT_KEY).setValue(scenario.getLoopCount());
        scenarioRef.child(Global.FIREBASE_LIGHT_OPEN_KEY).setValue(scenario.isLightOpen()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if(task.isSuccessful()){
                  AppUtils.showToastMessage(ScenarioSettingsActivity.this,getString(R.string.scenario_updated), R.drawable.ic_done, R.color.green_500);
                  finishActivity();
              }else{
                  AppUtils.showToastMessage(ScenarioSettingsActivity.this, getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
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
        Intent resultIntent = new Intent();

        Gson gson = new Gson();
        resultIntent.putExtra(Global.SCENARIO_KEY, gson.toJson(scenario));
        resultIntent.putExtra(Global.SCENARIO_DELETED_KEY, scenarioDeleted);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}