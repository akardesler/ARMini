/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 06:12
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hkardesler.armini.databinding.FragmentJoystickBinding;
import com.hkardesler.armini.helpers.JoyStickSurfaceView;

public class JoystickFragment extends Fragment {

    FragmentJoystickBinding binding;
    public JoystickFragment() {
        // Required empty public constructor
    }

    public static JoystickFragment newInstance() {
        return new JoystickFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJoystickBinding.inflate(inflater, container, false);


        joystickEvents();
        return binding.getRoot();
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
}