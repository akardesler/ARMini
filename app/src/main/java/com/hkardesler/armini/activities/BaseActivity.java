/*
 * *
 *  * Created by Haydar Kardesler on 6.06.2022 02:30
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.models.User;

public abstract class BaseActivity extends AppCompatActivity {

    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor;
    protected FirebaseAuth mAuth;
    protected FirebaseDatabase firebaseDatabase;
    protected StorageReference storageReference;
    protected Gson gson;
    protected User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = AppUtils.getPrefs(this);
        editor = prefs.edit();
        gson = new Gson();
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = AppUtils.getUser(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        setListeners();
    }

    protected abstract void setListeners();

}