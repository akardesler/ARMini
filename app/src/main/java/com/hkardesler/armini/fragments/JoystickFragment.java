/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 06:12
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.hkardesler.armini.databinding.FragmentJoystickBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.helpers.JoyStickSurfaceView;
import com.hkardesler.armini.impls.ControllerModeChangeListener;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.User;

import java.lang.reflect.Type;

public class JoystickFragment extends Fragment {

    FragmentJoystickBinding binding;
    Position mPosition;
    private static final String ARG_POSITION = "position";
    private static final String ARG_NEW_POSITION = "isNewPosition";
    private static final String ARG_SCENARIO_ID = "scenarioId";
    private ActivityResultLauncher<Intent> activityResultLauncher;
    DatabaseReference positionRef;
    String scenarioId;
    boolean isNewPosition = true;
    ControllerModeChangeListener mControllerModeChangeListener;
    int deletePositionId = -1;

    public JoystickFragment() {}
    public JoystickFragment(ControllerModeChangeListener controllerModeChangeListener) {
        mControllerModeChangeListener = controllerModeChangeListener;

    }

    public static JoystickFragment newInstance(String positionJson, boolean newPosition, String scenarioId, ControllerModeChangeListener controllerModeChangeListener) {
        JoystickFragment fragment = new JoystickFragment(controllerModeChangeListener);
        Bundle args = new Bundle();
        args.putString(ARG_POSITION, positionJson);
        args.putBoolean(ARG_NEW_POSITION, newPosition);
        args.putString(ARG_SCENARIO_ID, scenarioId);
        fragment.setArguments(args);

        return fragment;
    }

    public static JoystickFragment newInstance(ControllerModeChangeListener controllerModeChangeListener) {
        return new JoystickFragment(controllerModeChangeListener);
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

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button even
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJoystickBinding.inflate(inflater, container, false);

        binding.txtTitle.setText(getString(R.string.position_name, (mPosition.getKey())+1));
        binding.sliderGripper.setValue(mPosition.getGripper());
        binding.txtGripperValue.setText(String.valueOf(mPosition.getGripper()));
        setListeners();

        joystickEvents();
        return binding.getRoot();
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

        binding.rltJoystickMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.imgHelpRobotBaseShoulder.getVisibility() == View.VISIBLE){
                    binding.imgHelpRobotBaseShoulder.setVisibility(View.GONE);
                    binding.imgHelpRobotElbow.setVisibility(View.GONE);
                    binding.imgHelpRobotWrist.setVisibility(View.GONE);

                    binding.joystickView1.setVisibility(View.VISIBLE);
                    binding.joystickView2.setVisibility(View.VISIBLE);
                    binding.joystickView3.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positionRef.child(Global.FIREBASE_MOTOR_SPEED_KEY).setValue(Global.MOTOR_SPEED_POSITION_VALUE.getIntValue());
                positionRef.child(Global.FIREBASE_BASE_KEY).setValue(mPosition.getBase());
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
    }

    private void joystickEvents() {

        binding.joystickView1.setOnJoyStickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStick state) {

            }
        }, JoyStickSurfaceView.LOOP_INTERVAL_SLOW, JoyStickSurfaceView.LOOP_INTERVAL_FAST);

        binding.joystickView1.setOnLongPushListener(new JoyStickSurfaceView.OnLongPushListener() {
            @Override
            public void onLongPush() {
                Log.d("MainEvent", "long pushed");
            }
        });

        binding.joystickView1.setOnChangeStateListener(new JoyStickSurfaceView.OnChangeStateListener() {
            @Override
            public void onChangeState(JoyStickSurfaceView.JoyStick next,
                                      JoyStickSurfaceView.JoyStick previous) {

            }
        });

    }

    private void goToHomePosition(){
        AppUtils.goToHomePosition(mPosition);
        binding.sliderGripper.setValue(mPosition.getGripper());
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
}