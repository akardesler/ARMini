/*
 * *
 *  * Created by Haydar Kardesler on 5.06.2022 03:05
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityMainBinding;
import com.hkardesler.armini.databinding.ActivityPositionBinding;
import com.hkardesler.armini.fragments.SliderFragment;

public class PositionActivity extends AppCompatActivity {

    ActivityPositionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPositionBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SliderFragment.newInstance())
                    .commitNow();
        }

    }


}