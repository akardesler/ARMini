/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 03:04
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.activities.PositionSettingsActivity;
import com.hkardesler.armini.databinding.FragmentSliderBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.Scenario;
import com.hkardesler.armini.models.User;

import java.lang.reflect.Type;

public class SliderFragment extends Fragment {

    FragmentSliderBinding binding;
    Position mPosition;
    private static final String ARG_POSITION = "position";
    private static final String ARG_NEW_POSITION = "isNewPosition";
    private static final String ARG_SCENARIO_ID = "scenarioId";
    private ActivityResultLauncher<Intent> activityResultLauncher;
    DatabaseReference positionRef;
    String scenarioId;
    boolean isNewPosition = true;

    public SliderFragment() {
        // Required empty public constructor
    }

    public static SliderFragment newInstance(String positionJson, boolean newPosition, String scenarioId) {
        SliderFragment fragment = new SliderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POSITION, positionJson);
        args.putBoolean(ARG_NEW_POSITION, newPosition);
        args.putString(ARG_SCENARIO_ID, scenarioId);
        fragment.setArguments(args);
        return fragment;
    }

    public static SliderFragment newInstance() {
        return new SliderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Gson gson = new Gson();

            String json = getArguments().getString(ARG_POSITION);
            Type type = new TypeToken<Position>() {}.getType();
            mPosition = gson.fromJson(json, type);

            isNewPosition = getArguments().getBoolean(ARG_NEW_POSITION);
            scenarioId = getArguments().getString(ARG_SCENARIO_ID);
        }

        User user = AppUtils.getUser(getContext());
        positionRef = FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY)
                .child(user.getUserId()).child(Global.FIREBASE_SCENARIOS_KEY).child(scenarioId).child(Global.FIREBASE_POSITIONS_KEY)
                .child(String.valueOf(mPosition.getKey()));

        initActivityResultLauncher();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSliderBinding.inflate(inflater, container, false);
        binding.txtTitle.setText(getString(R.string.position_name, (mPosition.getKey())+1));
        setListeners();
        setInitialSliderValues();

        return binding.getRoot();
    }

    private void setListeners(){

        binding.btnBack.setOnClickListener(view -> requireActivity().finish());

        binding.cardHomePos.setOnClickListener(view -> goToHomePosition());

        binding.cardTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                i.putExtra(Global.SCENARIO_ID_KEY, scenarioId);
                activityResultLauncher.launch(i);
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positionRef.child(Global.FIREBASE_MOTOR_SPEED_KEY).setValue(Global.MOTOR_SPEED_POSITION_VALUE);
                positionRef.child(Global.FIREBASE_BASE_KEY).setValue(mPosition.getBase());
                positionRef.child(Global.FIREBASE_SHOULDER_KEY).setValue(mPosition.getShoulder());
                positionRef.child(Global.FIREBASE_ELBOW_VER_KEY).setValue(mPosition.getElbowVertical());
                positionRef.child(Global.FIREBASE_ELBOW_HOR_KEY).setValue(mPosition.getElbowHorizontal());
                positionRef.child(Global.FIREBASE_WRIST_VER_KEY).setValue(mPosition.getWristVertical());
                positionRef.child(Global.FIREBASE_WRIST_HOR_KEY).setValue(mPosition.getWristHorizontal());
                positionRef.child(Global.FIREBASE_GRIPPER_KEY).setValue(mPosition.getGripper()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        getActivity().finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AppUtils.showToastMessage(getActivity(), getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                    }
                });
            }
        });

        binding.sliderBase.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setBase(Math.round(value));
            binding.txtBaseValue.setText(String.valueOf(Math.round(value)));
        });

        binding.sliderShoulder.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setShoulder(Math.round(value));
            binding.txtShoulderValue.setText(String.valueOf(Math.round(value)));
        });

        binding.sliderElbowVer.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setElbowVertical(Math.round(value));
            binding.txtElbowVerValue.setText(String.valueOf(Math.round(value)));
        });

        binding.sliderElbowHor.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setElbowHorizontal(Math.round(value));
            binding.txtElbowHorValue.setText(String.valueOf(Math.round(value)));
        });

        binding.sliderWristVer.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setWristVertical(Math.round(value));
            binding.txtWristVerValue.setText(String.valueOf(Math.round(value)));
        });

        binding.sliderWristHor.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setWristHorizontal(Math.round(value));
            binding.txtWristHorValue.setText(String.valueOf(Math.round(value)));
        });

        binding.sliderGripper.addOnChangeListener((slider, value, fromUser) -> {
            mPosition.setGripper(Math.round(value));
            binding.txtGripperValue.setText(String.valueOf(Math.round(value)));
        });

    }

    private void setInitialSliderValues(){
        binding.sliderBase.setValue(mPosition.getBase());
        binding.sliderShoulder.setValue(mPosition.getShoulder());
        binding.sliderElbowVer.setValue(mPosition.getElbowVertical());
        binding.sliderElbowHor.setValue(mPosition.getElbowHorizontal());
        binding.sliderWristVer.setValue(mPosition.getWristVertical());
        binding.sliderWristHor.setValue(mPosition.getWristHorizontal());
        binding.sliderGripper.setValue(mPosition.getGripper());

        binding.txtBaseValue.setText(String.valueOf(mPosition.getBase()));
        binding.txtShoulderValue.setText(String.valueOf(mPosition.getShoulder()));
        binding.txtElbowVerValue.setText(String.valueOf(mPosition.getElbowVertical()));
        binding.txtElbowHorValue.setText(String.valueOf(mPosition.getElbowHorizontal()));
        binding.txtWristVerValue.setText(String.valueOf(mPosition.getWristVertical()));
        binding.txtWristHorValue.setText(String.valueOf(mPosition.getWristHorizontal()));
        binding.txtGripperValue.setText(String.valueOf(mPosition.getGripper()));


    }

    private void goToHomePosition(){
        AppUtils.goToHomePosition(mPosition);
        binding.sliderBase.setValue(mPosition.getBase());
        binding.sliderShoulder.setValue(mPosition.getShoulder());
        binding.sliderElbowVer.setValue(mPosition.getElbowVertical());
        binding.sliderElbowHor.setValue(mPosition.getElbowHorizontal());
        binding.sliderWristVer.setValue(mPosition.getWristVertical());
        binding.sliderWristHor.setValue(mPosition.getWristHorizontal());
        binding.sliderGripper.setValue(mPosition.getGripper());
    }

    private void initActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            boolean deletePosition = data.getBooleanExtra(Global.POSITION_DELETED_KEY, false);
                            if(deletePosition){
                                getActivity().finish();
                            }else{
                                Gson gson = new Gson();
                                String json = data.getStringExtra(Global.POSITION_KEY);
                                Type type = new TypeToken<Position>() {}.getType();
                                mPosition = gson.fromJson(json, type);
                            }
                        }
                    }
                });
    }
}