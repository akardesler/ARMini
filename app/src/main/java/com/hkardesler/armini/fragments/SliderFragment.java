/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 03:04
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.FragmentSliderBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.Scenario;

import java.lang.reflect.Type;

public class SliderFragment extends Fragment {

    FragmentSliderBinding binding;
    Position mPosition;
    Position prevPosition;
    boolean newPosition = false;
    private static final String ARG_POSITION = "position";
    private static final String ARG_PREV_POSITION = "prevPosition";


    public SliderFragment() {
        // Required empty public constructor
    }

    public static SliderFragment newInstance(String positionJson, String prevPositionJson) {
        SliderFragment fragment = new SliderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POSITION, positionJson);
        args.putString(ARG_PREV_POSITION, prevPositionJson);
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

            String jsonPrev = getArguments().getString(ARG_PREV_POSITION);
            prevPosition = gson.fromJson(jsonPrev, type);
        }else{
            mPosition = new Position();
            prevPosition = new Position();
            AppUtils.homePosition(prevPosition);
            newPosition = true;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSliderBinding.inflate(inflater, container, false);
        setListeners();
        if(newPosition){
            goToHomePosition();
        }
        setFirstValueTexts();

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

        binding.sliderBase.addOnChangeListener((slider, value, fromUser) ->
                binding.txtBaseValue.setText(String.valueOf(Math.round(value))));

        binding.sliderShoulder.addOnChangeListener((slider, value, fromUser) ->
                binding.txtShoulderValue.setText(String.valueOf(Math.round(value))));

        binding.sliderElbowVer.addOnChangeListener((slider, value, fromUser) ->
                binding.txtElbowVerValue.setText(String.valueOf(Math.round(value))));

        binding.sliderElbowHor.addOnChangeListener((slider, value, fromUser) ->
                binding.txtElbowHorValue.setText(String.valueOf(Math.round(value))));

        binding.sliderWristVer.addOnChangeListener((slider, value, fromUser) ->
                binding.txtWristVerValue.setText(String.valueOf(Math.round(value))));

        binding.sliderWristHor.addOnChangeListener((slider, value, fromUser) ->
                binding.txtWristHorValue.setText(String.valueOf(Math.round(value))));

        binding.sliderGripper.addOnChangeListener((slider, value, fromUser) ->
                binding.txtGripperValue.setText(String.valueOf(Math.round(value))));
    }


    private void goToHomePosition(){
        AppUtils.homePosition(mPosition);
        binding.sliderBase.setValue(mPosition.getBaseValue());
        binding.sliderShoulder.setValue(mPosition.getShoulderValue());
        binding.sliderElbowVer.setValue(mPosition.getElbowVerticalValue());
        binding.sliderElbowHor.setValue(mPosition.getElbowHorizontalValue());
        binding.sliderWristVer.setValue(mPosition.getWristVerticalValue());
        binding.sliderWristHor.setValue(mPosition.getWristHorizontalValue());
        binding.sliderGripper.setValue(mPosition.getGripperValue());
    }

    private void setFirstValueTexts(){
        binding.txtBaseValue.setText(String.valueOf(mPosition.getBaseValue()));
        binding.txtShoulderValue.setText(String.valueOf(mPosition.getShoulderValue()));
        binding.txtElbowVerValue.setText(String.valueOf(mPosition.getElbowVerticalValue()));
        binding.txtElbowHorValue.setText(String.valueOf(mPosition.getElbowHorizontalValue()));
        binding.txtWristVerValue.setText(String.valueOf(mPosition.getWristVerticalValue()));
        binding.txtWristHorValue.setText(String.valueOf(mPosition.getWristHorizontalValue()));
        binding.txtGripperValue.setText(String.valueOf(mPosition.getGripperValue()));
    }
}