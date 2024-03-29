/*
 * *
 *  * Created by Alper Kardesler on 26.05.2022 00:47
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 26.05.2022 00:47
 *
 */

package com.hkardesler.armini.activities;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hkardesler.armini.R;
import com.hkardesler.armini.adapters.ScenarioAdapter;
import com.hkardesler.armini.databinding.ActivityMainBinding;
import com.hkardesler.armini.databinding.LayoutContentMainBinding;
import com.hkardesler.armini.helpers.MQTTBroker;
import com.hkardesler.armini.impls.MqttBrokerCallback;
import com.hkardesler.armini.impls.ScenarioItemClickListener;
import com.hkardesler.armini.models.ArmInfo;
import com.hkardesler.armini.models.ArminiStatusEnum;
import com.hkardesler.armini.models.Scenario;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.helpers.TileDrawable;
import com.hkardesler.armini.models.WorkingModeEnum;
import com.squareup.picasso.Picasso;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends BaseActivity implements ScenarioItemClickListener, MqttBrokerCallback {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private LayoutContentMainBinding mainContent;
    private RecyclerView rvScenario;
    private ScenarioAdapter scenarioAdapter;
    private ArrayList<Scenario> scenarios;
    private DatabaseReference scenarioRef;
    ValueEventListener scenarioValueListener;
    DatabaseReference infoReference;
    final int intervalCheckArmStatus = 10000;
    final int armStatusCallbackTimeout = 5000;
    Handler handlerTimeoutStatus = new Handler();
    Handler handlerArmStatus = new Handler();
    MQTTBroker mqttBroker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainContent = binding.includeMain;
        rvScenario = mainContent.rvScenario;
        rvScenario.setAdapter(null);

        scenarioRef = firebaseDatabase.getReference(Global.FIREBASE_USERS_KEY).child(user.getUserId()).child(Global.FIREBASE_SCENARIOS_KEY);
        infoReference = FirebaseDatabase.getInstance().getReference(Global.FIREBASE_INFO_KEY);

        drawerLayout = binding.drawerLayout;
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        storageReference = FirebaseStorage.getInstance().getReference();



        addScenarioValueEventListenerFirebase();
        getUserImageFromFirebase(user.getUserId());
        initActivityResultLauncher();

        FrameLayout layoutPattern = binding.navView.getHeaderView(0).findViewById(R.id.layoutPattern);
        Drawable d = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pattern);
        layoutPattern.setBackground(new TileDrawable(d, Shader.TileMode.REPEAT));

        if(user != null){
            TextView txtHeaderTitle = binding.navView.getHeaderView(0).findViewById(R.id.txtHeaderTitle);
            txtHeaderTitle.setText(user.getFullName());

            TextView txtHeaderDesc = binding.navView.getHeaderView(0).findViewById(R.id.txtHeaderDesc);
            txtHeaderDesc.setText(user.getEmail());
        }

        AppUtils.addTextGradient(mainContent.txtTitle, getString(R.string.scenarios));
        addInfoValueEventListener();
         mqttBroker = new MQTTBroker();
         mqttBroker.setCallback(this);

    }

    @Override
    protected void setListeners() {
        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id=menuItem.getItemId();
                if (id == R.id.nav_signout){
                   signOut();
                } else if (id == R.id.nav_profile){
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    activityResultLauncher.launch(intent);
                }else if(id == R.id.nav_report_bug){
                    reportBugViaEmail();
                }else if(id == R.id.nav_about){
                    showAboutPopup();
                    drawerLayout.closeDrawers();
                }else if(id == R.id.nav_manual_control){
                    if(ArmInfo.getStatus() == ArminiStatusEnum.AVAILABLE){
                        Intent intent = new Intent(MainActivity.this, ManualControlActivity.class);
                        startActivity(intent);
                    }else if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY){
                        AppUtils.showToastMessage(MainActivity.this, getString(R.string.armini_busy), R.drawable.ic_error, R.color.blue_500);

                    }else if(ArmInfo.getStatus() == ArminiStatusEnum.OFFLINE){
                        AppUtils.showToastMessage(MainActivity.this, getString(R.string.armini_offline), R.drawable.ic_error, R.color.blue_500);

                    }
                }
                new Handler().postDelayed(() -> {
                    drawerLayout.closeDrawers();
                }, 500);
                return true;
            }


        });

        mainContent.btnAddScenario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddScenarioPopup();
            }
        });


        mainContent.btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.drawerLayout.openDrawer(GravityCompat.START, true);
            }
        });

        mainContent.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUtils.sendMessageViaMQTT(mqttBroker.getClient(), Global.MQTT_STOP_SCENARIO_TOPIC, "");
                AppUtils.updateInfoOnFirebase(ArminiStatusEnum.AVAILABLE, "", "");
                AppUtils.showToastMessage(MainActivity.this,getString(R.string.scenario_stopped), R.drawable.ic_done, R.color.green_500);

            }
        });

    }


    // It's triggered when a value is changed in info node on Firebase
    private void addInfoValueEventListener(){

        infoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int status = Objects.requireNonNull(snapshot.child(Global.FIREBASE_INFO_ARM_STATUS_KEY).getValue(Long.class)).intValue();
                ArmInfo.setOperatorId(snapshot.child(Global.FIREBASE_INFO_OPERATOR_ID_KEY).getValue(String.class));
                ArmInfo.setScenarioId(snapshot.child(Global.SCENARIO_ID_KEY).getValue(String.class));
                if(status == ArminiStatusEnum.OFFLINE.getIntValue()){
                    ArmInfo.setStatus(ArminiStatusEnum.OFFLINE);
                }else if(status == ArminiStatusEnum.AVAILABLE.getIntValue()){
                    ArmInfo.setStatus(ArminiStatusEnum.AVAILABLE);
                }else if(status == ArminiStatusEnum.BUSY.getIntValue()){
                    ArmInfo.setStatus(ArminiStatusEnum.BUSY);
                }
                updateStatusLayout();
                if(ArmInfo.getStatus() != ArminiStatusEnum.OFFLINE){
                    checkArmStatus();
                }else{
                    handlerArmStatus.removeCallbacksAndMessages(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkArmStatus(){
        handlerArmStatus.removeCallbacksAndMessages(null);
        handlerArmStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppUtils.sendMessageViaMQTT(mqttBroker.getClient(), Global.MQTT_ARM_STATUS_REQUEST_TOPIC, "");
                handlerTimeoutStatus.removeCallbacksAndMessages(null);
                handlerTimeoutStatus.postDelayed(() -> {
                    AppUtils.updateInfoOnFirebase(ArminiStatusEnum.OFFLINE, "", "");
                    handlerTimeoutStatus.removeCallbacksAndMessages(null);
                }, armStatusCallbackTimeout);
                handlerArmStatus.postDelayed(this, intervalCheckArmStatus);

            }
        }, intervalCheckArmStatus);

    }

    private void updateStatusLayout(){
        if(ArmInfo.getStatus() == ArminiStatusEnum.OFFLINE){
            mainContent.statusAnimation.setAnimation("robot_offline.json");
            mainContent.statusAnimation.playAnimation();
            mainContent.animationView.setAnimation("offline.json");
            mainContent.animationView.cancelAnimation();
            mainContent.txtStatus.setText(getString(R.string.offline));
            mainContent.cardOperator.setVisibility(View.GONE);
            mainContent.cardStop.setVisibility(View.GONE);
            setOperatorImage(user.getUserId());
        }else if(ArmInfo.getStatus() == ArminiStatusEnum.AVAILABLE){
            mainContent.statusAnimation.setAnimation("robot_available.json");
            mainContent.statusAnimation.playAnimation();
            mainContent.animationView.setAnimation("connection.json");
            mainContent.animationView.setProgress(0f);
            mainContent.animationView.pauseAnimation();
            mainContent.txtStatus.setText(getString(R.string.available));
            mainContent.cardOperator.setVisibility(View.GONE);
            mainContent.cardStop.setVisibility(View.GONE);
            setOperatorImage(user.getUserId());
        }else if(ArmInfo.getStatus() == ArminiStatusEnum.BUSY){
            mainContent.statusAnimation.setAnimation("robot_working.json");
            mainContent.statusAnimation.playAnimation();
            mainContent.animationView.setAnimation("connection.json");
            mainContent.animationView.playAnimation();
            mainContent.txtStatus.setText(getString(R.string.busy));
            mainContent.cardOperator.setVisibility(View.VISIBLE);
            if(user.getUserId().equals(ArmInfo.getOperatorId())){
                mainContent.cardStop.setVisibility(View.VISIBLE);
            }else{
                mainContent.cardStop.setVisibility(View.GONE);
            }
            setOperatorImage(ArmInfo.getOperatorId());
            setOperatorName(ArmInfo.getOperatorId());
        }
    }

    private void reportBugViaEmail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"hkardesler1@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "ARMini - Report Bug");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut() {
        mAuth.signOut();
        AppUtils.putBool(this, Global.SIGNED_IN_KEY, false);
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAddScenarioPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_add_scenario_popup, null);
        EditText inputScenarioName = dialogView.findViewById(R.id.input_scenario_name);
        Button btnAddDialog = dialogView.findViewById(R.id.btnAdd);
        Button btnDialogCancel = dialogView.findViewById(R.id.btnCancel);

        inputScenarioName.setText(getString(R.string.new_scenario_name, scenarios.size()+1));

        builder.setView(dialogView);
        builder.create();
        final AlertDialog ad = builder.show();
        inputScenarioName.requestFocus();
        ad.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        inputScenarioName.selectAll();

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

        btnAddDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isExist = false;
                for(int i = 0; i < scenarios.size(); i++){
                    if(scenarios.get(i).getName().equals(inputScenarioName.getText().toString())){
                        isExist = true;
                        break;
                    }
                }
                if(!inputScenarioName.getText().toString().trim().equals("")){
                    if(!isExist){
                        addScenarioToFirebase(inputScenarioName.getText().toString());
                        ad.dismiss();
                    }else{
                        inputScenarioName.setError(getString(R.string.scenario_name_exist));
                    }
                }else{
                    inputScenarioName.setError(getString(R.string.scenario_name_warning));
                }
            }
        });
    }

    private void addScenarioToFirebase(String name) {
        scenarioRef.removeEventListener(scenarioValueListener);
        String scenarioKey = scenarioRef.push().getKey();
        assert scenarioKey != null;

        if(scenarios == null){
            scenarios = new ArrayList<>();
        }
        Scenario scenario = new Scenario(scenarioKey, name, 0, WorkingModeEnum.INFINITE, 1, false);

        scenarioRef.child(scenarioKey).child(Global.FIREBASE_SCENARIO_NAME_KEY).setValue(scenario.getName());
        scenarioRef.child(scenarioKey).child(Global.FIREBASE_WORKING_MODE_KEY).setValue(scenario.getWorkingMode().getIntValue());
        scenarioRef.child(scenarioKey).child(Global.FIREBASE_LOOP_COUNT_KEY).setValue(scenario.getLoopCount());
        scenarioRef.child(scenarioKey).child(Global.FIREBASE_LIGHT_OPEN_KEY).setValue(scenario.isLightOpen())

                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            String json = AppUtils.convertJson(scenario);
                            Intent i = new Intent(MainActivity.this, ScenarioActivity.class);
                            i.putExtra(Global.SCENARIO_KEY, json);
                            activityResultLauncher.launch(i);
                            addScenarioValueEventListenerFirebase();
                        }else{
                            AppUtils.showToastMessage(MainActivity.this, getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                        }
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            boolean refreshActivity = data.getBooleanExtra(Global.REFRESH_MAIN_ACTIVITY_KEY, false);
                            if(refreshActivity){
                                finish();
                                startActivity(getIntent());
                            }

                            boolean refreshProfilePhoto = data.getBooleanExtra(Global.REFRESH_PROFILE_PHOTO, false);
                            if(refreshProfilePhoto){
                                getUserImageFromFirebase(user.getUserId());
                            }
                        }
                    }
                });
    }

    private void showAboutPopup(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_popup_about, null);
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        TextView txtBitbucketLink = dialogView.findViewById(R.id.txtGitHubLink);
        txtBitbucketLink.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(dialogView);
        builder.create();
        final AlertDialog ad = builder.show();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

    }

    private void getUserImageFromFirebase(String userId){
        View navHeader = binding.navView.getHeaderView(0);
        ImageView imgProfilePhoto = navHeader.findViewById(R.id.imgProfilePhoto);
        StorageReference fileRef = storageReference.child(Global.FIREBASE_USER_IMAGES_KEY).child(userId+".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imgProfilePhoto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imgProfilePhoto.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_profile_person));
            }
        });
    }

    private void setOperatorImage(String operatorId){
        StorageReference fileRef = storageReference.child(Global.FIREBASE_USER_IMAGES_KEY).child(operatorId+".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mainContent.imgProfilePhoto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mainContent.imgProfilePhoto.setImageDrawable(AppCompatResources.getDrawable(MainActivity.this, R.drawable.ic_profile_person));
            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        String json = AppUtils.convertJson(scenarios.get(position));
        Intent i = new Intent(MainActivity.this, ScenarioActivity.class);
        i.putExtra(Global.SCENARIO_KEY, json);
        activityResultLauncher.launch(i);
    }

    // When a scenario added, it's triggered and update scenario list and Recycler View.
    private void addScenarioValueEventListenerFirebase(){
        scenarioValueListener = scenarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                scenarios = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Scenario scenario = new Scenario();
                    scenario.setId(postSnapshot.getKey());
                    scenario.setName(postSnapshot.child(Global.FIREBASE_SCENARIO_NAME_KEY).getValue(String.class));
                    scenario.setLightOpen(Boolean.TRUE.equals(postSnapshot.child(Global.FIREBASE_LIGHT_OPEN_KEY).getValue(Boolean.class)));
                    scenario.setLoopCount(Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_LOOP_COUNT_KEY).getValue(Long.class)).intValue());
                    int workingMode = Objects.requireNonNull(postSnapshot.child(Global.FIREBASE_WORKING_MODE_KEY).getValue(Long.class)).intValue();
                    if(workingMode == WorkingModeEnum.INFINITE.getIntValue()){
                        scenario.setWorkingMode(WorkingModeEnum.INFINITE);
                    }else if(workingMode == WorkingModeEnum.LOOP.getIntValue()){
                        scenario.setWorkingMode(WorkingModeEnum.LOOP);
                    }

                    scenario.setPositionCount((int)postSnapshot
                            .child(Global.FIREBASE_POSITIONS_KEY).getChildrenCount());

                    scenarios.add(scenario);
                }
                setRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mainContent.txtListEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setRecyclerView() {
        mainContent.cardviewLoading.setVisibility(View.GONE);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvScenario.setLayoutManager(gridLayoutManager);
        Collections.reverse(scenarios);
        scenarioAdapter = new ScenarioAdapter(this, scenarios, this);
        rvScenario.setAdapter(scenarioAdapter);

        if(scenarios.size() > 0){
            mainContent.txtListEmpty.setVisibility(View.GONE);
        }else{
            mainContent.txtListEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void setOperatorName(String operatorId){
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY).child(operatorId).child(Global.FIREBASE_FULL_NAME_KEY);
        databaseReference.child(Global.FIREBASE_FULL_NAME_KEY).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                   binding.includeMain.txtOperatorName.setText(task.getResult().getValue(String.class));
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                }
            }
        });
    }

    //MQTT BROKER CALLBACK
    @Override
    public void onComplete() {
        mqttBroker.getClient().subscribeWith()
                .topicFilter(Global.MQTT_ARM_STATUS_CALLBACK_TOPIC)
                .send();
        mqttBroker.getClient().toAsync().publishes(ALL, publish -> {
            // This method gets callback from MQTT topics
            if(publish.getTopic().toString().equals(Global.MQTT_ARM_STATUS_CALLBACK_TOPIC)){
                handlerTimeoutStatus.removeCallbacksAndMessages(null);
            }
        });
    }

    @Override
    public void onError() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerArmStatus.removeCallbacksAndMessages(null);
        handlerTimeoutStatus.removeCallbacksAndMessages(null);
    }
}
