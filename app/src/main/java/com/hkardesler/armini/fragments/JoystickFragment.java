/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 06:12
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hkardesler.armini.R;

public class JoystickFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_joystick, container, false);
    }
}